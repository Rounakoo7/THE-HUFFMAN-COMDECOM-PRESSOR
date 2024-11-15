import axios from "axios";

const API = axios.create({
    baseURL: "http://localhost:1010",
    responseType: "arraybuffer",
});

export default API;