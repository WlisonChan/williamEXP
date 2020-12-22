package org.csu.util;

import lombok.extern.slf4j.Slf4j;

import java.io.FileWriter;
import java.io.IOException;

@Slf4j
public class WriterUtil {

    private String outPutUrl = "src/main/resources/";
    private String filename = "";
    FileWriter writer ;

    public WriterUtil(String filename){
        this.filename = filename;
        outPutUrl = outPutUrl+filename;
        try {
            writer = new FileWriter(outPutUrl);
        } catch (IOException e) {
            log.error("file output error！");
        }
    }
    public void outputData(String str){
        if (writer==null){
            log.error("writer has not init");
        }
        try {
            writer.write(str+"\n");
        } catch (IOException e) {
            log.error("file output error！");
        }
    }

    public void close(){
        if (writer!=null) {
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
