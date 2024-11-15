import React from 'react'
import DragAndDrop from './DragAndDrop';

function Home() {
  return (
    <div style={{ fontFamily: "Cursive", fontStyle: "oblique", fontWeight: "bold" }}>
      <h1 style={{ color: "#6defc4", paddingLeft: "50px", paddingTop: "30px"}}>THE HUFFMAN COM-DECOMPRESSOR</h1>
      <div style={{ paddingLeft: "1000px" }}>
        <div className="box">
          <DragAndDrop />
        </div>
      </div>
    </div>
  )
}

export default Home
