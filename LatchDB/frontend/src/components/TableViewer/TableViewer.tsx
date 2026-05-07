import React, { useEffect, useState } from 'react';
import { getTables, query } from '../../api';

const TableViewer: React.FC = () => {
    const [tables, setTables] = useState<string[]>([]);
    const [selectedTable, setSelectedTable] = useState('');
    const [data, setData] = useState<any[]>([]);

    useEffect(() => {
        const fetchTables = async () => {
            const list = await getTables();
            setTables(list);
            if (list.length > 0) setSelectedTable(list[0]);
        };
        fetchTables();
    }, []);

    useEffect(() => {
        if (selectedTable) {
            const fetchData = async () => {
                const results = await query(`SELECT * FROM ${selectedTable}`);
                setData(results);
            };
            fetchData();
        }
    }, [selectedTable]);

    return (
        <div className="table-viewer">
            <h2>Table Viewer</h2>
            <select value={selectedTable} onChange={(e) => setSelectedTable(e.target.value)}>
                {tables.map(t => <option key={t} value={t}>{t}</option>)}
            </select>
            <div className="table-container">
                {data.length > 0 ? (
                    <table>
                        <thead>
                            <tr>
                                {Object.keys(data[0]).map(key => <th key={key}>{key}</th>)}
                            </tr>
                        </thead>
                        <tbody>
                            {data.map((row, i) => (
                                <tr key={i}>
                                    {Object.values(row).map((val: any, j) => <td key={j}>{val}</td>)}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : <p>No data found in table</p>}
            </div>
        </div>
    );
};

export default TableViewer;
