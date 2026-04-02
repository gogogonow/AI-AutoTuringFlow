#!/usr/bin/env python3
# -*- coding: utf-8 -*-
"""
光模块数据解析脚本
用于从 CSV、Excel 等格式导入数据到系统
"""

import csv
import json
import requests
import argparse
import sys
from datetime import datetime
from typing import List, Dict, Any


class ModuleDataParser:
    """光模块数据解析器"""
    
    def __init__(self, api_base_url: str = "http://localhost:8080/api"):
        self.api_base_url = api_base_url
        self.session = requests.Session()
        self.session.headers.update({'Content-Type': 'application/json'})
    
    def parse_csv(self, file_path: str) -> List[Dict[str, Any]]:
        """
        解析 CSV 文件
        
        Args:
            file_path: CSV 文件路径
            
        Returns:
            解析后的模块数据列表
        """
        modules = []
        
        try:
            with open(file_path, 'r', encoding='utf-8') as csvfile:
                reader = csv.DictReader(csvfile)
                
                for row in reader:
                    module = self._normalize_module_data(row)
                    if module:
                        modules.append(module)
                        
            print(f"成功解析 {len(modules)} 条记录")
            return modules
            
        except FileNotFoundError:
            print(f"错误: 文件不存在 - {file_path}")
            return []
        except Exception as e:
            print(f"解析 CSV 文件时出错: {str(e)}")
            return []
    
    def _normalize_module_data(self, row: Dict[str, str]) -> Dict[str, Any]:
        """
        标准化模块数据
        
        Args:
            row: 原始数据行
            
        Returns:
            标准化后的模块数据
        """
        try:
            module = {
                'code': row.get('code', '').strip(),
                'status': row.get('status', 'Active').strip(),
                'vendor': row.get('vendor', '').strip() or None,
                'processStatus': row.get('process_status', '').strip() or None,
                'LD': row.get('ld', '').strip() or None,
                'PD': row.get('pd', '').strip() or None,
                'remarks': row.get('remarks', '').strip() or None,
            }
            
            # 验证必填字段
            if not module['code']:
                print(f"警告: 跳过无效记录 - 缺少 code 字段")
                return None
            
            return module
            
        except Exception as e:
            print(f"标准化数据时出错: {str(e)}")
            return None
    
    def upload_modules(self, modules: List[Dict[str, Any]]) -> Dict[str, int]:
        """
        上传模块数据到 API
        
        Args:
            modules: 模块数据列表
            
        Returns:
            统计信息 {'success': 成功数, 'failed': 失败数}
        """
        stats = {'success': 0, 'failed': 0}
        
        for idx, module in enumerate(modules, 1):
            try:
                response = self.session.post(
                    f"{self.api_base_url}/modules",
                    json=module
                )
                
                if response.status_code == 201:
                    stats['success'] += 1
                    print(f"[{idx}/{len(modules)}] 成功创建模块: {module['code']}")
                else:
                    stats['failed'] += 1
                    error_msg = response.json().get('error', response.text)
                    print(f"[{idx}/{len(modules)}] 创建失败: {module['code']} - {error_msg}")
                    
            except requests.exceptions.RequestException as e:
                stats['failed'] += 1
                print(f"[{idx}/{len(modules)}] 网络错误: {module['code']} - {str(e)}")
            except Exception as e:
                stats['failed'] += 1
                print(f"[{idx}/{len(modules)}] 未知错误: {module['code']} - {str(e)}")
        
        return stats
    
    def export_modules_to_csv(self, output_file: str) -> bool:
        """
        从 API 导出模块数据到 CSV
        
        Args:
            output_file: 输出文件路径
            
        Returns:
            是否成功
        """
        try:
            response = self.session.get(f"{self.api_base_url}/modules")
            
            if response.status_code != 200:
                print(f"获取数据失败: {response.status_code}")
                return False
            
            modules = response.json()
            
            if not modules:
                print("没有数据可导出")
                return False
            
            # 定义 CSV 字段
            fieldnames = ['code', 'status', 'vendor', 'processStatus', 
                         'LD', 'PD', 'remarks', 'createTime']
            
            with open(output_file, 'w', newline='', encoding='utf-8') as csvfile:
                writer = csv.DictWriter(csvfile, fieldnames=fieldnames)
                writer.writeheader()
                
                for module in modules:
                    row = {k: module.get(k, '') for k in fieldnames}
                    writer.writerow(row)
            
            print(f"成功导出 {len(modules)} 条记录到 {output_file}")
            return True
            
        except Exception as e:
            print(f"导出数据时出错: {str(e)}")
            return False


def main():
    """主函数"""
    parser = argparse.ArgumentParser(description='光模块数据解析工具')
    parser.add_argument('action', choices=['import', 'export'], 
                       help='操作类型: import(导入) 或 export(导出)')
    parser.add_argument('file', help='文件路径')
    parser.add_argument('--api-url', default='http://localhost:8080/api',
                       help='API 基础 URL (默认: http://localhost:8080/api)')
    
    args = parser.parse_args()
    
    parser_obj = ModuleDataParser(api_base_url=args.api_url)
    
    if args.action == 'import':
        print(f"开始导入数据从: {args.file}")
        modules = parser_obj.parse_csv(args.file)
        
        if not modules:
            print("没有有效数据可导入")
            sys.exit(1)
        
        print(f"\n准备上传 {len(modules)} 条记录...\n")
        stats = parser_obj.upload_modules(modules)
        
        print(f"\n导入完成!")
        print(f"成功: {stats['success']}")
        print(f"失败: {stats['failed']}")
        
    elif args.action == 'export':
        print(f"开始导出数据到: {args.file}")
        success = parser_obj.export_modules_to_csv(args.file)
        
        if not success:
            sys.exit(1)


if __name__ == '__main__':
    main()
