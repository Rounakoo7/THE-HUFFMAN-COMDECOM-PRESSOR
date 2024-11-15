import React, { useRef, useState } from 'react';
import { ImCross } from "react-icons/im";
import axios from './Axios';
import { toast } from 'react-toastify';
import { useNavigate } from 'react-router-dom';
import { RotatingLines } from 'react-loader-spinner'

const DragAndDrop = () => {
    const navigate = useNavigate();
    const wrapperRef = useRef(null);
    const [file, setFile] = useState('');
    const [loader, setLoader] = useState(0);
    const [submit, setSubmit] = useState(0);
    const [responseSize, setResponseSize] = useState(0);
    const onDragEnter = () => wrapperRef.current.classList.add('dragover');
    const onDragLeave = () => wrapperRef.current.classList.remove('dragover');
    const onDrop = () => wrapperRef.current.classList.remove('dragover');
    function timeoutAndRefresh(delay) {
        setTimeout(() => {
            navigate(0);
        }, delay)
    }
    const onFileDrop = (e) => {
        setFile(e.target.files[0]);
    }
    const fileRemove = () => {
        setFile('');
    }
    function download(response, submit) {
        const url = window.URL.createObjectURL(new Blob([response.data], { type: "application/octet-stream" }));
        const link = document.createElement('a');
        link.href = url;
        let name = file.name.substring(0,file.name.length - 4);
        if(submit === 1){
            name = name + "_compressed.txt"
        }
        else if(submit === 2){
            name = name +"_decompressed.txt"
        }
        link.setAttribute('download', name);
        document.body.appendChild(link);
        link.click();
    }
    const handleCompress = async (event) => {
        event.preventDefault();
        const formData = new FormData();
        formData.append("file", file);
        try {
            setLoader(1);
            const response = await axios.post("/compress", formData, {
                headers: {
                    "Content-Type": "application/octet-stream",
                },
            });
            if (response.status === 200) {
                setSubmit(1);
                download(response, 1);
                setResponseSize(response.data.byteLength);
                setLoader(2);
                toast.success("File compressed successfully");
            }
        }
        catch (error) {
            timeoutAndRefresh(3000);
            if (error.message.substring(error.message.length - 3, error.message.length) === "406") {
                toast.error("Only compression of text files is supported");
            }
            else if (error.message.substring(error.message.length - 3, error.message.length) === "404") {
                toast.error("File is too small to be benifitted by compression");
            }
            else if (error.message.substring(error.message.length - 3, error.message.length) === "500") {
                toast.error("The file does not have data in the form of text");
            }
            else {
                toast.error("Server error. Please try again later");
            }
        }
    }
    const handleDecompress = async (event) => {
        event.preventDefault();
        const formData = new FormData();
        formData.append("file", file);
        try {
            setLoader(1);
            const response = await axios.post("/decompress", formData, {
                headers: {
                    "Content-Type": "application/octet-stream",
                },
            });
            if (response.status === 200) {
                setSubmit(2);
                setResponseSize(response.data.byteLength);
                download(response, 2);
                setLoader(2);
                toast.success("File decompressed successfully");
            }
            else {
                const errorData = await response.json()
                toast.error(errorData.message);
            }
        }
        catch (error) {
            timeoutAndRefresh(3000);
            if (error.message.substring(error.message.length - 3, error.message.length) === "406") {
                toast.error("Only decompression of text files is supported");
            }
            else if (error.message.substring(error.message.length - 3, error.message.length) === "404") {
                toast.error("Some error occurred during decompression. Please try again");
            }
            else if (error.message.substring(error.message.length - 3, error.message.length) === "500") {
                toast.error("The file was either tampered or not compressed on this website");
            }
            else {
                toast.error("Server error. Please try again later");
            }
        }
    }
    return (<>
        {loader === 0 ? <><div className="drag-and-drop-file-input" ref={wrapperRef} onDragEnter={onDragEnter} onDragLeave={onDragLeave} onDrop={onDrop}>
            <div className="drag-and-drop-file-input__label">
                <img src="/upload.png" alt="" />
                <p>Drag & Drop your file here</p>
            </div>
            <input type="file" value="" onChange={onFileDrop} />
        </div>
            {file !== '' ? (
                <div className="drag-and-drop-file-preview">
                    <p className="drag-and-drop-file-preview__title" style={{ color: "#a1ff59" }}>
                        Ready to upload
                    </p>
                    {<><div className="drag-and-drop-file-preview__item">
                        <img src="/file.png" alt="" />
                        <div className="drag-and-drop-file-preview__item__info">
                            <p>{file.name}</p>
                            <p>{file.size} Bytes</p>
                        </div>
                        <span className="drag-and-drop-file-preview__item__del" onClick={() => fileRemove()}><ImCross /></span>
                    </div>
                        <div>
                            <form>
                                <span style={{ paddingLeft: "36px" }}></span>
                                <button type="submit" className='submit' style={{ background: "transparent", color: "white", height: "135px", width: "135px" }}><img src="/compress.png" alt="" style={{ height: "100px", width: "110px" }} onClick={handleCompress} /><br />COMPRESS</button>
                                <span style={{ paddingLeft: "58px" }}></span>
                                <button type="submit" className='submit' style={{ background: "transparent", color: "white", height: "135px", width: "135px" }}><img src="/decompress.png" alt="" style={{ height: "100px", width: "110px" }} onClick={handleDecompress} /><br />DECOMPRESS</button>
                            </form>
                        </div></>
                    }
                </div>) : null
            }</> : <></>}
        {loader === 1 ? <>
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <div style={{ paddingLeft: "150px" }}>
                <RotatingLines
                    visible={true}
                    height="96"
                    width="96"
                    color="grey"
                    strokeWidth="5"
                    animationDuration="0.75"
                    ariaLabel="rotating-lines-loading"
                    wrapperStyle={{}}
                    wrapperClass=""
                />
            </div>
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
        </> : <></>}
        {loader === 2 ? <>
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            {submit === 1 ? <><div style={{ textAlign: "center", color: "#a1ff59" }}>
                <p>ORIGINAL FILE SIZE : {file.size} Bytes</p>
                <p>COMPRESSED FILE SIZE : {responseSize} Bytes</p>
                <p>COMPRESSION RATIO : {(file.size / responseSize).toFixed(2)}</p>
                <form>
                    <button type="submit" className='btn btn-danger' onClick={() => {setFile('');setResponseSize(0);setSubmit(0);setLoader(0);}}>BACK</button>
                </form>
            </div>
            </> : <></>}
            {submit === 2 ? <><div style={{ textAlign: "center", color: "#a1ff59" }}>
                <p>ORIGINAL FILE SIZE : {file.size} Bytes</p>
                <p>DECOMPRESSED FILE SIZE : {responseSize} Bytes</p>
                <p>DECOMPRESSION RATIO : {(responseSize / file.size).toFixed(2)}</p>
                <form>
                    <button type="submit" className='btn btn-danger' onClick={() => {setFile('');setResponseSize(0);setSubmit(0);setLoader(0);}}>BACK</button>
                </form>
            </div>
            </> : <></>}
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
            <br />
        </> : <></>
        }</>
    );
}

export default DragAndDrop;