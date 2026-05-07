import React, { useState, useEffect } from 'react';
import { BarChart, Bar, XAxis, YAxis, CartesianGrid, Tooltip, Legend, ResponsiveContainer, LineChart, Line } from 'recharts';
import { query, getTables } from '../../api';

const AnalyticsDashboard: React.FC = () => {
    const [tables, setTables] = useState<string[]>([]);
    const [selectedTable, setSelectedTable] = useState('');
    const [data, setData] = useState<any[]>([]);
    const [analytics, setAnalytics] = useState<any>(null);

    useEffect(() => {
        const fetchTables = async () => {
            const list = await getTables();
            setTables(list);
            if (list.length > 0) setSelectedTable(list[0]);
        };
        fetchTables();
    }, []);

    const runAnalytics = async () => {
        if (!selectedTable) return;
        const allData = await query(`SELECT * FROM ${selectedTable} LIMIT 100`);
        setData(allData);

        // Run some stats if there are numeric columns
        // For demonstration, let's assume we want AVG of the first numeric column
        // In a real system, we'd let the user pick.
    };

    return (
        <div className="analytics-dashboard">
            <h2>Analytics Dashboard</h2>
            <div className="controls">
                <select value={selectedTable} onChange={(e) => setSelectedTable(e.target.value)}>
                    {tables.map(t => <option key={t} value={t}>{t}</option>)}
                </select>
                <button onClick={runAnalytics}>Visualize</button>
            </div>

            <div className="charts">
                {data.length > 0 && (
                    <div style={{ width: '100%', height: 300 }}>
                        <h3>Data Distribution</h3>
                        <ResponsiveContainer>
                            <BarChart data={data}>
                                <CartesianGrid strokeDasharray="3 3" />
                                <XAxis dataKey={Object.keys(data[0])[0]} />
                                <YAxis />
                                <Tooltip />
                                <Legend />
                                <Bar dataKey={Object.keys(data[0])[Object.keys(data[0]).length - 1]} fill="#8884d8" />
                            </BarChart>
                        </ResponsiveContainer>
                    </div>
                )}
            </div>
        </div>
    );
};

export default AnalyticsDashboard;
