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

package com.tulskiy.musique.audio;

import com.tulskiy.musique.audio.formats.cue.CUEParser;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.util.Util;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.tag.Tag;

import java.io.*;
import java.util.List;

/**
 * @Author: Denis Tulskiy
 * @Date: 25.06.2009
 */
public abstract class AudioFileReader {
    private static CUEParser cueParser;

    public void read(File f, List<Song> list) {
        Song audioFile = readSingle(f);
        String cueSheet = audioFile.getCueSheet();
        if (cueSheet != null && cueSheet.length() > 0) {
            if (cueParser == null)
                cueParser = new CUEParser();
            LineNumberReader reader = new LineNumberReader(new StringReader(cueSheet));
            cueParser.parse(list, audioFile, reader, true);
        } else {
            list.add(audioFile);
        }
    }

    public abstract Song readSingle(File file);

    public abstract boolean isFileSupported(String ext);

    public abstract Decoder getDecoder();

    protected void copyTagFields(Tag abstractTag, Song song, File file) throws IOException {
        if (abstractTag != null && song != null) {
            song.setAlbum(abstractTag.getFirstAlbum());
            song.setArtist(abstractTag.getFirstArtist());
            song.setComment(abstractTag.getFirstComment());
            song.setTitle(abstractTag.getFirstTitle());
            song.setYear(abstractTag.getFirstYear());
            song.setCueSheet(abstractTag.getFirst("CUESHEET"));
            song.setGenre(abstractTag.getFirstGenre());
            song.setAlbumArtist(abstractTag.getFirst("ALBUM ARTIST"));
            song.setTrackNumber(abstractTag.getFirstTrack());


//            for (Artwork art : abstractTag.getArtworkList()) {
//                    tag.addAlbumart(art.getImage());
//            }
        }
    }

    protected void copyHeaderFields(GenericAudioHeader header, Song song) {
        if (header != null && song != null) {
            song.setBitrate((int) header.getBitRateAsNumber());
            song.setChannels(header.getChannelNumber());
            song.setCodec(header.getEncodingType());
            song.setTotalSamples((long) (header.getPreciseLength() * header.getSampleRateAsNumber()));
            song.setSamplerate(header.getSampleRateAsNumber());
            song.setStartPosition(0);
        }
    }
}