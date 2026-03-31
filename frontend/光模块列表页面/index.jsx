import React from 'react';
import Header from './components/Header';
import FilterPanel from './components/FilterPanel';
import ActionBar from './components/ActionBar';
import DataTable from './components/DataTable';
import Pagination from './components/Pagination';
import './styles.css';

const ModuleListPage = () => {
  return (
    <div className="module-list-page">
      <Header title="光模块管理" />
      <FilterPanel />
      <ActionBar />
      <DataTable />
      <Pagination />
    </div>
  );
};

export default ModuleListPage;