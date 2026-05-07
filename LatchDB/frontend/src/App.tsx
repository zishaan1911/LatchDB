import React, { useState } from 'react';
import SQLEditor from './components/SQLEditor/SQLEditor';
import TableViewer from './components/TableViewer/TableViewer';
import SchemaManager from './components/SchemaManager/SchemaManager';
import AnalyticsDashboard from './components/AnalyticsDashboard/AnalyticsDashboard';
import { Database, Table, Layout, PieChart } from 'lucide-react';
import './App.css';

function App() {
  const [activeTab, setActiveTab] = useState('sql');

  return (
    <div className="app-container">
      <div className="sidebar">
        <h1>LatchDB</h1>
        <nav>
          <button 
            className={activeTab === 'sql' ? 'active' : ''} 
            onClick={() => setActiveTab('sql')}
          >
            <Database size={20} /> SQL Editor
          </button>
          <button 
            className={activeTab === 'tables' ? 'active' : ''} 
            onClick={() => setActiveTab('tables')}
          >
            <Table size={20} /> Table Viewer
          </button>
          <button 
            className={activeTab === 'schema' ? 'active' : ''} 
            onClick={() => setActiveTab('schema')}
          >
            <Layout size={20} /> Schema Manager
          </button>
          <button 
            className={activeTab === 'analytics' ? 'active' : ''} 
            onClick={() => setActiveTab('analytics')}
          >
            <PieChart size={20} /> Analytics
          </button>
        </nav>
      </div>
      <main className="content">
        {activeTab === 'sql' && <SQLEditor />}
        {activeTab === 'tables' && <TableViewer />}
        {activeTab === 'schema' && <SchemaManager />}
        {activeTab === 'analytics' && <AnalyticsDashboard />}
      </main>
    </div>
  );
}

export default App;
