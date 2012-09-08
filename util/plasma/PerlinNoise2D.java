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
public class PerlinNoise2D {

    private static double LACUNARITY = 2.1379201;
    private static double H = 0.836281;

    private double[] _spectralWeights2d;

    private final int[] _noisePermutations;
    private boolean _recomputeSpectralWeights2d = true;
    private int _octaves = 1;

    /**
     * Init. a new generator with a given seed value.
     *
     * @param seed The seed value
     */
    public PerlinNoise2D(int seed) {
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
     * Returns the noise2d value at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axis
     * @return The noise2d value
     */
    public double noise2d(double x, double y) {
        int X = (int) fastFloor(x) & 255, Y = (int) fastFloor(y) & 255;

        x -= fastFloor(x);
        y -= fastFloor(y);

        double u = fade(x), v = fade(y);

        double[] g00=grad2d(X,Y);
        double[] g01=grad2d(X+1,Y);
        double[] g10=grad2d(X,Y+1);
        double[] g11=grad2d(X+1,Y+1);

        double w00=g00[0]*x+g00[1]*y;
        double w01=g01[0]*x+g01[1]*y;
        double w10=g10[0]*x+g10[1]*y;
        double w11=g11[0]*x+g11[1]*y;

        double w0=lerp(u,w00,w01);
        double w1=lerp(u,w10,w11);

        return lerp(v,w0,w1);
    }

    /**
     * Returns Fractional Brownian Motion at the given position.
     *
     * @param x Position on the x-axis
     * @param y Position on the y-axi
     * @return The fBm2d value
     */
    public double fBm2d(double x, double y, double LACUNARITY, double H) {
        double result = 0.0;

        if (_recomputeSpectralWeights2d) {
            _spectralWeights2d = new double[_octaves];

            for (int i = 0; i < _octaves; i++)
                _spectralWeights2d[i] = Math.pow(LACUNARITY, -H * i);

            _recomputeSpectralWeights2d = false;
        }

        for (int i = 0; i < _octaves; i++) {
            result += noise2d(x, y) * _spectralWeights2d[i];

            x *= LACUNARITY;
            y *= LACUNARITY;
        }

        return result;
    }

    public double fBm2d(double x, double z) {    //default noise
        double result = 0.0;

        if (_recomputeSpectralWeights2d) {
            _spectralWeights2d = new double[_octaves];

            for (int i = 0; i < _octaves; i++)
                _spectralWeights2d[i] = Math.pow(LACUNARITY, -H * i);

            _recomputeSpectralWeights2d = false;
        }

        for (int i = 0; i < _octaves; i++) {
            result += noise2d(x, z) * _spectralWeights2d[i];

            x *= LACUNARITY;
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

    private double[] grad2d(int x, int y) {
        double[] h = hash(x,y);
        double[] result = new double[2];
        result[0]=2*h[0]-1;
        result[1]=2*h[1]-1;
        return result;
    }

    private int h8 (int u, int v) {
        u = u & 0xFF;
        v = v & 0xFF;
        return _noisePermutations[_noisePermutations[u]+v];
    }

    private double[] hash(int u, int v) {
        final int M = 0x3F;
        int h = h8(u, v);
        double hx = h & M; // use bits 0..5 for hx()
        double hy = (h >> 2) & M; // use bits 2..7 for hy()
        return new double[] {hx/M, hy/M};
    }

    public void setOctaves(int octaves) {
        _octaves = octaves;
        _recomputeSpectralWeights2d = true;
    }

    public int getOctaves() {
        return _octaves;
    }

    public void setLACUNARITY(double L) {
        LACUNARITY = L;
        _recomputeSpectralWeights2d = true;
    }

    public double getLACUNARITY() {
        return LACUNARITY;
    }

    public void setPersistence(double h) {
        H = h;
        _recomputeSpectralWeights2d = true;
    }

    public double getPersistence() {
        return H;
    }
}


/**  public void Erode(float smoothness)
 {
 for (int i = 1; i < Size - 1; i++)
 {
 for (int j = 1; j < Size - 1; j++)
 {
 float d_max = 0.0f;
 int[] match = { 0, 0 };

 for (int u = -1; u <= 1; u++)
 {
 for (int v = -1; v <= 1; v++)
 {
 if(Math.Abs(u) + Math.Abs(v) > 0)
 {
 float d_i = Heights[i, j] - Heights[i + u, j + v];
 if (d_i > d_max)
 {
 d_max = d_i;
 match[0] = u; match[1] = v;
 }
 }
 }
 }

 if(0 < d_max && d_max <= (smoothness / (float)Size))
 {
 float d_h = 0.5f * d_max;
 Heights[i, j] -= d_h;
 Heights[i + match[0], j + match[1]] += d_h;
 }
 }
 }
 } **/