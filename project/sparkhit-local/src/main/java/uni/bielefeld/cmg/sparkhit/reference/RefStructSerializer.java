package uni.bielefeld.cmg.sparkhit.reference;


import uni.bielefeld.cmg.sparkhit.io.TextFileBufferInput;
import uni.bielefeld.cmg.sparkhit.io.TextFileBufferOutput;
import uni.bielefeld.cmg.sparkhit.serializer.kryoSerializer;
import uni.bielefeld.cmg.sparkhit.struct.BinaryBlock;
import uni.bielefeld.cmg.sparkhit.struct.Block;
import uni.bielefeld.cmg.sparkhit.struct.KmerLoc;
import uni.bielefeld.cmg.sparkhit.struct.RefTitle;
import uni.bielefeld.cmg.sparkhit.util.DefaultParam;
import uni.bielefeld.cmg.sparkhit.util.InfoDumper;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.sql.Ref;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rhinempi on 22.07.2017.
 *
 *       Sparkhit
 *
 * Copyright (c) 2017.
 *       Liren Huang     <huanglr at cebitec.uni-bielefeld.de>
 *
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */


public class RefStructSerializer implements RefSerializer {
    private DefaultParam param;
    private kryoSerializer kSerializer = new kryoSerializer();
    private RefStructBuilder ref;
    private InfoDumper info = new InfoDumper();

    public void setParameter(DefaultParam param){
        this.param = param;
    }

    public RefStructBuilder getStruct(){
        return this.ref;
    }

    public void kryoDeserialization(){
        String bbl = param.inputFaPath + ".bbl";
        String blo = param.inputFaPath + ".blo";
        String tit = param.inputFaPath + ".tit";
        String ind = param.inputFaPath + ".ind";
        String met = param.inputFaPath + ".met";

        ref = new RefStructBuilder();
        ref.setParameter(param);

        File BBListFile = new File(bbl);
        if (BBListFile.exists()){
            ref.BBList = (List<BinaryBlock>) kSerializer.kryoDeSerialization(ArrayList.class, bbl);
        }else{
            info.readMessage("reference data index : " + bbl + " is missing");
            info.screenDump();
        }

        File blockFile = new File(blo);
        if (blockFile.exists()){
            ref.block = (List<Block>) kSerializer.kryoDeSerialization(ArrayList.class, blo);
        }else{
            info.readMessage("reference data index : " + blo + " is missing");
            info.screenDump();
        }

        File titleFile = new File(tit);
        if (titleFile.exists()){
            ref.title = (List<RefTitle>) kSerializer.kryoDeSerialization(ArrayList.class, tit);
        }else{
            info.readMessage("reference data index : " + tit + " is missing");
            info.screenDump();
        }

        File indexFile = new File(ind);
        if (indexFile.exists()){
            ref.index = (KmerLoc[]) kSerializer.kryoDeSerialization(KmerLoc[].class, ind);
        }else{
            info.readMessage("reference data index : " + ind + " is missing");
            info.screenDump();
        }

        File metaFile = new File(met);
        if (metaFile.exists()){
            readMetaData(met);
        }else{
            info.readMessage("reference data index : " + met + " is missing");
            info.screenDump();
        }

    }

    public void kryoSerialization() {
        String bbl = param.inputFaPath + ".bbl";
        String blo = param.inputFaPath + ".blo";
        String tit = param.inputFaPath + ".tit";
        String ind = param.inputFaPath + ".ind";
        String met = param.inputFaPath + ".met";

        kSerializer.kryoSerialization(ref.BBList, bbl, 0);
        kSerializer.kryoSerialization(ref.block, blo, 0);
        kSerializer.kryoSerialization(ref.title, tit, 0);
        kSerializer.kryoSerialization(ref.index, ind, 1);
        putMetaData(met);

    }

    private void readMetaData(String met){
        TextFileBufferInput metaDataTextFileReader = new TextFileBufferInput();
        metaDataTextFileReader.setInput(met);
        BufferedReader metReader = metaDataTextFileReader.getBufferReader();

        try {
            String line;
            while((line = metReader.readLine()) !=null){
                if (line.startsWith("Reference total length : ")) {
                    ref.totalLength = Long.parseLong(line.split("\\t")[1]);
                } else if (line.startsWith("Reference total contig number : ")){
                    ref.totalNum = Integer.parseInt(line.split("\\t")[1]);
                }
            }
            metReader.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void putMetaData(String met){

        /* put total length and contig number in .met (meta) file */
        TextFileBufferOutput metaDataTextFileWriter = new TextFileBufferOutput();
        metaDataTextFileWriter.setOutput(met, false);
        BufferedWriter metWriter = metaDataTextFileWriter.getOutputBufferWriter();

        try {
            metWriter.write("Reference total length : \t" + ref.totalLength + "\n");
            metWriter.write("Reference total contig number : \t" + ref.totalNum + "\n");
            metWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void setStruct(RefStructBuilder ref){
        this.ref = ref;
    }
}
