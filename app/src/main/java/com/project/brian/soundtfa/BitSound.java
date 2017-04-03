package com.project.brian.soundtfa;

import android.util.Log;

import java.io.InputStream;
import java.util.Iterator;

public class BitSound implements SoundThread.SoundIterator {
    final static int START_HZ = 15000;
    final static int STEP_HZ = 250;
    final static int BITS = 4;

    final static int HANDSHAKE_START_HZ = 19000;
    final static int HANDSHAKE_END_HZ = 19000 + 512;

    final int file_size;
    final InputStream stream;

    public BitSound(InputStream stream, int file_size) {
        this.stream = stream;
        this.file_size = file_size;
    }

    @Override
    public Iterator<Integer> iterator() {
        final Iterator<Integer> bits_iterator = new BitIterator(stream, BITS).iterator();
        return new Iterator<Integer>() {
            boolean yield_start = false;
            boolean yield_end = false;

            @Override
            public boolean hasNext() {
                if (!yield_start || !yield_end) {
                    return true;
                }

                return bits_iterator.hasNext();
            }

            @Override
            public Integer next() {
                if (!yield_start) {
                    yield_start = true;
                    return HANDSHAKE_START_HZ;
                }

                if (!yield_end && !bits_iterator.hasNext()) {
                    yield_end = true;
                    return HANDSHAKE_END_HZ;
                }

                Integer step = bits_iterator.next();
                Log.e("DEBUG", "chunk: " + step + ", hz: " + (START_HZ + step * STEP_HZ));
                return START_HZ + step * STEP_HZ;
            }

            @Override
            public void remove() {
            }
        };
    }

    @Override
    public int size() {
        // +2 for handshake
        return Math.round(file_size * ((float) Byte.SIZE) / BITS) + 2;
    }
}
