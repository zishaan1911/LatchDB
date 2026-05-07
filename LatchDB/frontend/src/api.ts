import axios from 'axios';

const API_BASE_URL = 'http://localhost:8080';

export const query = async (sql: string) => {
    const response = await axios.post(`${API_BASE_URL}/query`, { sql });
    return response.data;
};

export const getTables = async () => {
    const response = await axios.get(`${API_BASE_URL}/tables`);
    return response.data;
};

export const createTable = async (name: string, columns: any[]) => {
    const response = await axios.post(`${API_BASE_URL}/table/create`, { name, columns });
    return response.data;
};

export const importCsv = async (tableName: string, csv: string) => {
    const response = await axios.post(`${API_BASE_URL}/import/csv`, { tableName, csv });
    return response.data;
};
