import React, { useState } from 'react';
import { createTable } from '../../api';

const SchemaManager: React.FC = () => {
    const [tableName, setTableName] = useState('');
    const [columns, setColumns] = useState([{ name: 'id', type: 'INT', primaryKey: true }]);

    const addColumn = () => {
        setColumns([...columns, { name: '', type: 'STRING', primaryKey: false }]);
    };

    const handleCreate = async () => {
        await createTable(tableName, columns);
        alert('Table created successfully');
    };

    return (
        <div className="schema-manager">
            <h2>Schema Manager</h2>
            <input 
                placeholder="Table Name" 
                value={tableName} 
                onChange={(e) => setTableName(e.target.value)} 
            />
            <div className="columns">
                {columns.map((col, i) => (
                    <div key={i} className="column-row">
                        <input 
                            placeholder="Name" 
                            value={col.name} 
                            onChange={(e) => {
                                const newCols = [...columns];
                                newCols[i].name = e.target.value;
                                setColumns(newCols);
                            }} 
                        />
                        <select 
                            value={col.type} 
                            onChange={(e) => {
                                const newCols = [...columns];
                                newCols[i].type = e.target.value;
                                setColumns(newCols);
                            }}
                        >
                            <option value="INT">INT</option>
                            <option value="STRING">STRING</option>
                            <option value="DOUBLE">DOUBLE</option>
                        </select>
                        <label>
                            PK
                            <input 
                                type="checkbox" 
                                checked={col.primaryKey} 
                                onChange={(e) => {
                                    const newCols = [...columns];
                                    newCols[i].primaryKey = e.target.checked;
                                    setColumns(newCols);
                                }}
                            />
                        </label>
                    </div>
                ))}
            </div>
            <button onClick={addColumn}>Add Column</button>
            <button onClick={handleCreate}>Create Table</button>
        </div>
    );
};

export default SchemaManager;
