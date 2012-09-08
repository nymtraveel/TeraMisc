/*
 * Copyright 2012 Benjamin Glatzel <benjamin.glatzel@me.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */


/**
 * Improved Perlin noise3d based on the reference implementation by Ken Perlin.
 *
 * @author Benjamin Glatzel <benjamin.glatzel@me.com>
 * @author Florian Rohm
 */
public class PerlinNoise3D {

    private static double LACUNARITY = 2.1379201;
    private static double H = 0.836281;

    private double[] _spectralWeights3d;

    private final int[] _noisePermutations;
    private boolean _recomputeSpectralWeights3d = true;
    private int _octaves = 9;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public PerlinNoise3D(int seed) {
        FastRandom rand = new FastRandom(seed);

        _noisePermutations = new int[512];
        int[] _noiseTable = new int[256];

        // Init. the noise3d table
        for (int i = 0; i < 256; i++)
            _noiseTable[i] = i;

        // Shuffle the array
        for (int i = 0; i < 256; i++) {
            int j = rand.randomInt() % 256;
            j = (j < 0) ? -j : j;

            int swap = _noiseTable[i];
            _noiseTable[i] = _noiseTable[j];
            _noiseTable[j] = swap;
        }

        // Finally replicate the noise3d permutations in the remaining 256 index positions
        for (int i = 0; i < 256; i++)
            _noisePermutations[i] = _noisePermutations[i + 256] = _noiseTable[i];

    }

    public static double fastFloor(double d) {
        int i = (int) d;
        return (d < 0 && d != i) ? i - 1 : i;
    }
    /**
     * Returns the noise3d value at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The noise3d value
     */
    public double noise3d(double x, double y, double z) {
        int X = (int) fastFloor(x) & 255, Y = (int) fastFloor(y) & 255, Z = (int) fastFloor(z) & 255;

        x -= fastFloor(x);
        y -= fastFloor(y);
        z -= fastFloor(z);

        double u = fade(x), v = fade(y), w = fade(z);
        int A = _noisePermutations[X] + Y, AA = _noisePermutations[A] + Z, AB = _noisePermutations[(A + 1)] + Z,
                B = _noisePermutations[(X + 1)] + Y, BA = _noisePermutations[B] + Z, BB = _noisePermutations[(B + 1)] + Z;

        return lerp(w, lerp(v, lerp(u, grad3d(_noisePermutations[AA], x, y, z),
                grad3d(_noisePermutations[BA], x - 1, y, z)),
                lerp(u, grad3d(_noisePermutations[AB], x, y - 1, z),
                        grad3d(_noisePermutations[BB], x - 1, y - 1, z))),
                lerp(v, lerp(u, grad3d(_noisePermutations[(AA + 1)], x, y, z - 1),
                        grad3d(_noisePermutations[(BA + 1)], x - 1, y, z - 1)),
                        lerp(u, grad3d(_noisePermutations[(AB + 1)], x, y - 1, z - 1),
                                grad3d(_noisePermutations[(BB + 1)], x - 1, y - 1, z - 1))));
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @param z Position on the z-axis
     * @return The fBm3d value
     */
    public double fBm3d(double x, double y, double z, double LACUNARITY, double H) {
        double result = 0.0;

        if (_recomputeSpectralWeights3d) {
            _spectralWeights3d = new double[_octaves];

            for (int i = 0; i < _octaves; i++)
                _spectralWeights3d[i] = Math.pow(LACUNARITY, -H * i);

            _recomputeSpectralWeights3d = false;
        }

        for (int i = 0; i < _octaves; i++) {
            result += noise3d(x, y, z) * _spectralWeights3d[i];

            x *= LACUNARITY;
            y *= LACUNARITY;
            z *= LACUNARITY;
        }

        return result;
    }

    public double fBm3d(double x, double y, double z) {
        double result = 0.0;

        if (_recomputeSpectralWeights3d) {
            _spectralWeights3d = new double[_octaves];

            for (int i = 0; i < _octaves; i++)
                _spectralWeights3d[i] = Math.pow(LACUNARITY, -H * i);

            _recomputeSpectralWeights3d = false;
        }

        for (int i = 0; i < _octaves; i++) {
            result += noise3d(x, y,z) * _spectralWeights3d[i];

            x *= LACUNARITY;
            y *= LACUNARITY;
            z *= LACUNARITY;
        }

        return result;
    }

    private static double fade(double t) {
        return t * t * t * (t * (t * 6 - 15) + 10);
    }

    private static double lerp(double t, double a, double b) {
        return a + t * (b - a);
    }

    private static double grad3d(int hash, double x, double y, double z) {
        int h = hash & 15;
        double u = h < 8 ? x : y, v = h < 4 ? y : h == 12 || h == 14 ? x : z;
        return ((h & 1) == 0 ? u : -u) + ((h & 2) == 0 ? v : -v);
    }

    public void setOctaves(int octaves) {
        _octaves = octaves;
        _recomputeSpectralWeights3d = true;
    }

    public int getOctaves() {
        return _octaves;
    }

    public void setLACUNARITY(double L) {
        LACUNARITY = L;
        _recomputeSpectralWeights3d = true;
    }

    public double getLACUNARITY() {
        return LACUNARITY;
    }

    public void setPersistence(double h) {
        H = h;
        _recomputeSpectralWeights3d = true;
    }

    public double getPersistence() {
        return H;
    }
}
