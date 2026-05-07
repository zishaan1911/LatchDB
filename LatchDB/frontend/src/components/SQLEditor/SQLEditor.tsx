import React, { useState } from 'react';
import { query } from '../../api';
import './SQLEditor.css';

const SQLEditor: React.FC = () => {
    const [sql, setSql] = useState('SELECT * FROM students');
    const [results, setResults] = useState<any[]>([]);
    const [error, setError] = useState('');

    const handleRun = async () => {
        try {
            setError('');
            const data = await query(sql);
            setResults(data);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Error executing query');
        }
    };

    return (
        <div className="sql-editor">
            <h2>SQL Editor</h2>
            <textarea 
                value={sql} 
                onChange={(e) => setSql(e.target.value)}
                placeholder="Enter SQL here..."
            />
            <button onClick={handleRun}>Run Query</button>
            {error && <div className="error">{error}</div>}
            <div className="results">
                {results.length > 0 ? (
                    <table>
                        <thead>
                            <tr>
                                {Object.keys(results[0]).map(key => <th key={key}>{key}</th>)}
                            </tr>
                        </thead>
                        <tbody>
                            {results.map((row, i) => (
                                <tr key={i}>
                                    {Object.values(row).map((val: any, j) => <td key={j}>{val}</td>)}
                                </tr>
                            ))}
                        </tbody>
                    </table>
                ) : <p>No results to display</p>}
            </div>
        </div>
    );
};

export default SQLEditor;
