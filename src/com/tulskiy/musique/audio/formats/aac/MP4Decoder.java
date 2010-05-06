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

package com.tulskiy.musique.audio.formats.aac;

import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.audio.formats.aac.libjfaad.libjfaad;
import com.tulskiy.musique.audio.io.PCMOutputStream;
import com.tulskiy.musique.playlist.Song;

import javax.sound.sampled.AudioFormat;
import java.nio.ByteBuffer;

/**
 * @Author: Denis Tulskiy
 * @Date: Oct 5, 2009
 */
public class MP4Decoder implements Decoder {
    private static libjfaad lib = new libjfaad();

    private int handler;
    private AudioFormat audioFormat;
    private byte[] buffer = new byte[5000];
    private PCMOutputStream outputStream;

    public boolean open(Song inputFile) {
        handler = lib.open(inputFile.getFile().getAbsolutePath());

        if (handler == -1) {
            return false;
        }

        audioFormat = new AudioFormat(lib.getSampleRate(handler), 16, lib.getChannels(handler), true, false);

        return true;
    }

    public void setOutputStream(PCMOutputStream outputStream) {
        this.outputStream = outputStream;
    }

    public AudioFormat getAudioFormat() {
        return audioFormat;
    }

    public void seekSample(long sample) {
        lib.seek(handler, sample);
    }

    public int decode(byte[] buf) {
//        if (len == -1)
//            return -1;

//        outputStream.write(buffer, 0, len);

        return lib.decode(handler, buf, buf.length);
    }

    public void close() {
        lib.close(handler);
    }
}