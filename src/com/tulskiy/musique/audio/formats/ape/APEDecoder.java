/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.audio.formats.ape;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.Song;
import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.JMACException;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 13.06.2009
 */
public class APEDecoder implements Decoder {
    static {
        System.setProperty("jmac.NATIVE", "true");
    }

    private IAPEDecompress decoder;
    private static final int BLOCKS_PER_DECODE = 4096 * 2;
    private int blockAlign;
    private PCMOutputStream outputStream;
    private byte[] buffer;

    public boolean open(Song inputFile) {
        try {
            File apeInputFile = File.createFile(inputFile.getFile().getAbsolutePath(), "r");
            decoder = IAPEDecompress.CreateIAPEDecompress(apeInputFile);
            blockAlign = decoder.getApeInfoBlockAlign();
            buffer = new byte[blockAlign * BLOCKS_PER_DECODE];
            return true;
        } catch (IOException e) {
            e.printStackTrace();
            close();
        }
        return false;
    }

    public void setOutputStream(PCMOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public AudioFormat getAudioFormat() {
        return new AudioFormat(decoder.getApeInfoSampleRate(),
                decoder.getApeInfoBitsPerSample(),
                decoder.getApeInfoChannels(),
                true,
                false);
    }

    public void seekSample(long sample) {
        try {
            if (decoder.getApeInfoDecompressCurrentBlock() != sample) {
                decoder.Seek((int) sample);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int decode(byte[] buf) {
        try {
//            System.out.println(decoder.getApeInfoDecompressCurrentBlock());
            int blocksDecoded = decoder.GetData(buf, BLOCKS_PER_DECODE);
            if (blocksDecoded <= 0)
                return -1;
            //            outputStream.write(buffer, 0, len);
            return blocksDecoded * blockAlign;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JMACException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public void close() {
        try {
            decoder.getApeInfoIoSource().close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}