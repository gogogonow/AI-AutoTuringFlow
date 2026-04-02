#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
批量数据处理脚本
支持大规模数据导入、数据校验和错误恢复
"""

import json
import time
import logging
from typing import List, Dict, Any, Tuple
from datetime import datetime
from data_parser import ModuleDataParser

# 配置日志
logging.basicConfig(
    level=logging.INFO,
    format='%(asctime)s - %(levelname)s - %(message)s',
    handlers=[
        logging.FileHandler(f'batch_import_{datetime.now().strftime("%Y%m%d_%H%M%S")}.log'),
        logging.StreamHandler()
    ]
)

logger = logging.getLogger(__name__)


class BatchProcessor:
    """批量处理器"""
    
    def __init__(self, api_base_url: str = "http://localhost:8080/api", 
                 batch_size: int = 100, retry_times: int = 3):
        """
        初始化批量处理器
        
        Args:
            api_base_url: API 基础 URL
            batch_size: 每批次处理的记录数
            retry_times: 失败重试次数
        """
        self.parser = ModuleDataParser(api_base_url)
        self.batch_size = batch_size
        self.retry_times = retry_times
        self.failed_records = []
    
    def validate_data(self, modules: List[Dict[str, Any]]) -> Tuple[List[Dict], List[Dict]]:
        """
        数据校验
        
        Args:
            modules: 待校验的模块数据列表
            
        Returns:
            (有效数据列表, 无效数据列表)
        """
        valid_data = []
        invalid_data = []
        
        for module in modules:
            errors = []
            
            # 检查必填字段
            if not module.get('code'):
                errors.append('缺少 code 字段')
            
            if not module.get('status'):
                errors.append('缺少 status 字段')
            
            # 检查字段长度
            if module.get('code') and len(module['code']) > 100:
                errors.append('code 字段长度超过限制')
            
            if errors:
                module['_errors'] = errors
                invalid_data.append(module)
                logger.warning(f"无效数据: {module.get('code', 'UNKNOWN')} - {', '.join(errors)}")
            else:
                valid_data.append(module)
        
        logger.info(f"数据校验完成: 有效 {len(valid_data)}, 无效 {len(invalid_data)}")
        return valid_data, invalid_data
    
    def process_in_batches(self, modules: List[Dict[str, Any]]) -> Dict[str, int]:
        """
        批量处理数据
        
        Args:
            modules: 模块数据列表
            
        Returns:
            统计信息
        """
        total = len(modules)
        stats = {'success': 0, 'failed': 0, 'skipped': 0}
        
        logger.info(f"开始批量处理 {total} 条记录，批次大小: {self.batch_size}")
        
        for i in range(0, total, self.batch_size):
            batch = modules[i:i + self.batch_size]
            batch_num = i // self.batch_size + 1
            total_batches = (total + self.batch_size - 1) // self.batch_size
            
            logger.info(f"处理批次 {batch_num}/{total_batches} ({len(batch)} 条记录)")
            
            for module in batch:
                success = self._upload_with_retry(module)
                
                if success:
                    stats['success'] += 1
                else:
                    stats['failed'] += 1
                    self.failed_records.append(module)
            
            # 批次间延迟，避免服务器过载
            if i + self.batch_size < total:
                time.sleep(0.5)
        
        logger.info(f"批量处理完成: 成功 {stats['success']}, 失败 {stats['failed']}")
        return stats
    
    def _upload_with_retry(self, module: Dict[str, Any]) -> bool:
        """
        带重试的上传
        
        Args:
            module: 模块数据
            
        Returns:
            是否成功
        """
        for attempt in range(self.retry_times):
            try:
                response = self.parser.session.post(
                    f"{self.parser.api_base_url}/modules",
                    json=module,
                    timeout=10
                )
                
                if response.status_code == 201:
                    logger.info(f"成功创建模块: {module['code']}")
                    return True
                elif response.status_code == 400:
                    # 客户端错误，不重试
                    error_msg = response.json().get('error', response.text)
                    logger.error(f"创建失败 (客户端错误): {module['code']} - {error_msg}")
                    return False
                else:
                    # 服务器错误，重试
                    logger.warning(f"创建失败 (尝试 {attempt + 1}/{self.retry_times}): "
                                 f"{module['code']} - 状态码 {response.status_code}")
                    
                    if attempt < self.retry_times - 1:
                        time.sleep(2 ** attempt)  # 指数退避
                        
            except Exception as e:
                logger.warning(f"上传异常 (尝试 {attempt + 1}/{self.retry_times}): "
                             f"{module['code']} - {str(e)}")
                
                if attempt < self.retry_times - 1:
                    time.sleep(2 ** attempt)
        
        logger.error(f"所有重试失败: {module['code']}")
        return False
    
    def save_failed_records(self, output_file: str = 'failed_records.json'):
        """
        保存失败的记录
        
        Args:
            output_file: 输出文件路径
        """
        if not self.failed_records:
            logger.info("没有失败的记录需要保存")
            return
        
        try:
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(self.failed_records, f, indent=2, ensure_ascii=False)
            
            logger.info(f"已保存 {len(self.failed_records)} 条失败记录到 {output_file}")
        except Exception as e:
            logger.error(f"保存失败记录时出错: {str(e)}")
    
    def process_file(self, file_path: str) -> Dict[str, int]:
        """
        处理文件
        
        Args:
            file_path: 文件路径
            
        Returns:
            统计信息
        """
        logger.info(f"开始处理文件: {file_path}")
        
        # 解析数据
        modules = self.parser.parse_csv(file_path)
        
        if not modules:
            logger.error("没有有效数据")
            return {'success': 0, 'failed': 0}
        
        # 数据校验
        valid_data, invalid_data = self.validate_data(modules)
        
        # 保存无效数据
        if invalid_data:
            invalid_file = f'invalid_data_{datetime.now().strftime("%Y%m%d_%H%M%S")}.json'
            with open(invalid_file, 'w', encoding='utf-8') as f:
                json.dump(invalid_data, f, indent=2, ensure_ascii=False)
            logger.info(f"已保存 {len(invalid_data)} 条无效数据到 {invalid_file}")
        
        # 批量处理
        stats = self.process_in_batches(valid_data)
        stats['invalid'] = len(invalid_data)
        
        # 保存失败记录
        if self.failed_records:
            self.save_failed_records()
        
        return stats


if __name__ == '__main__':
    import sys
    
    if len(sys.argv) < 2:
        print("用法: python batch_processor.py <csv_file> [api_url]")
        sys.exit(1)
    
    file_path = sys.argv[1]
    api_url = sys.argv[2] if len(sys.argv) > 2 else "http://localhost:8080/api"
    
    processor = BatchProcessor(api_base_url=api_url, batch_size=50, retry_times=3)
    
    start_time = time.time()
    stats = processor.process_file(file_path)
    elapsed_time = time.time() - start_time
    
    logger.info("\n" + "="*50)
    logger.info("处理完成!")
    logger.info(f"总耗时: {elapsed_time:.2f} 秒")
    logger.info(f"成功: {stats['success']}")
    logger.info(f"失败: {stats['failed']}")
    logger.info(f"无效: {stats.get('invalid', 0)}")
    logger.info("="*50)
