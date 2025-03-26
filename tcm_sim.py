import numpy as np
import matplotlib.pyplot as plt

# DFP block
def dfp(z, f):
    # pam5 symbol options
    pam5 = {-2, -1, 0, 1, 2}

    # initialize the a array
    a = np.zeros(f.size)

    # perform the dfp algorithm
    y = np.zeros(z.size)
    for i in range(z.size):
        y[i] = z[i] + (-1 * f[1:] * a[1:]).sum()
        d = min(pam5, key=lambda x: abs(x - (y[i] - f[0] * a[0])))
        a = np.roll(a, 1)
        a[0] = d

    return y

# 1d LA-BMU block
def la_bmu_1d(y, f1):
    # pam5 sybmols
    pam5 = {-2, -1, 0, 1, 2}
    A = {-1, 1}
    B = {-2, 0, 2}

    ya = np.zeros(pam5.size)
    yb = np.zeros(pam5.size)
    for i in range(pam5.size):
        ya[i] = y - min(A, key=lambda x: abs(x - (y[i] - f1 * pam5[i]))) - f1 * pam5[i]
        yb[i] = y - min(B, key=lambda x: abs(x - (y[i] - f1 * pam5[i]))) - f1 * pam5[i]
    return ya, yb

# muxu block
# sel is a set of 8 pam5 values
def muxu(ya, yb, sel):
    idx = sel + 2
    return ya[idx], yb[idx]

# 4d BMU block
def bmu_4d(ch1_ya, ch1_yb, ch2_ya, ch2_yb, ch3_ya, ch3_yb, ch4_ya, ch4_yb):
    s0_s0 = min(ch1_ya[0] + ch2_ya[0] + ch3_ya[0] + ch4_ya[0], ch1_yb[0] + ch2_yb[0] + ch3_yb[0] + ch4_yb[0]) # min of AAAA, BBBB
    s0_s2 = min(ch1_ya[0] + ch2_ya[0] + ch3_yb[0] + ch4_yb[0], ch1_yb[0] + ch2_yb[0] + ch3_ya[0] + ch4_ya[0]) # min of AABB, BBAA
    s0_s4 = min(ch1_ya[0] + ch2_yb[0] + ch3_yb[0] + ch4_ya[0], ch1_yb[0] + ch2_ya[0] + ch3_ya[0] + ch4_yb[0]) # min of ABBA, BAAB
    s0_s6 = min(ch1_ya[0] + ch2_yb[0] + ch3_ya[0] + ch4_yb[0], ch1_yb[0] + ch2_ya[0] + ch3_yb[0] + ch4_ya[0]) # min of ABAB, BABA

    s2_s0 = min(ch1_ya[2] + ch2_ya[2] + ch3_ya[2] + ch4_ya[2], ch1_yb[2] + ch2_yb[2] + ch3_yb[2] + ch4_yb[2]) # min of AAAA, BBBB
    s2_s2 = min(ch1_ya[2] + ch2_ya[2] + ch3_yb[2] + ch4_yb[2], ch1_yb[2] + ch2_yb[2] + ch3_ya[2] + ch4_ya[2]) # min of AABB, BBAA
    s2_s4 = min(ch1_ya[2] + ch2_yb[2] + ch3_yb[2] + ch4_ya[2], ch1_yb[2] + ch2_ya[2] + ch3_ya[2] + ch4_yb[2]) # min of ABBA, BAAB
    s2_s6 = min(ch1_ya[2] + ch2_yb[2] + ch3_ya[2] + ch4_yb[2], ch1_yb[2] + ch2_ya[2] + ch3_yb[2] + ch4_ya[2]) # min of ABAB, BABA

    s4_s0 = min(ch1_ya[4] + ch2_ya[4] + ch3_ya[4] + ch4_ya[4], ch1_yb[4] + ch2_yb[4] + ch3_yb[4] + ch4_yb[4]) # min of AAAA, BBBB
    s4_s2 = min(ch1_ya[4] + ch2_ya[4] + ch3_yb[4] + ch4_yb[4], ch1_yb[4] + ch2_yb[4] + ch3_ya[4] + ch4_ya[4]) # min of AABB, BBAA
    s4_s4 = min(ch1_ya[4] + ch2_yb[4] + ch3_yb[4] + ch4_ya[4], ch1_yb[4] + ch2_ya[4] + ch3_ya[4] + ch4_yb[4]) # min of ABBA, BAAB
    s4_s6 = min(ch1_ya[4] + ch2_yb[4] + ch3_ya[4] + ch4_yb[4], ch1_yb[4] + ch2_ya[4] + ch3_yb[4] + ch4_ya[4]) # min of ABAB, BABA

    s6_s0 = min(ch1_ya[6] + ch2_ya[6] + ch3_ya[6] + ch4_ya[6], ch1_yb[6] + ch2_yb[6] + ch3_yb[6] + ch4_yb[6]) # min of AAAA, BBBB
    s6_s2 = min(ch1_ya[6] + ch2_ya[6] + ch3_yb[6] + ch4_yb[6], ch1_yb[6] + ch2_yb[6] + ch3_ya[6] + ch4_ya[6]) # min of AABB, BBAA
    s6_s4 = min(ch1_ya[6] + ch2_yb[6] + ch3_yb[6] + ch4_ya[6], ch1_yb[6] + ch2_ya[6] + ch3_ya[6] + ch4_yb[6]) # min of ABBA, BAAB
    s6_s6 = min(ch1_ya[6] + ch2_yb[6] + ch3_ya[6] + ch4_yb[6], ch1_yb[6] + ch2_ya[6] + ch3_yb[6] + ch4_ya[6]) # min of ABAB, BABA

    s1_s1 = min(ch1_ya[1] + ch2_ya[1] + ch3_ya[1] + ch4_yb[1], ch1_yb[1] + ch2_yb[1] + ch3_yb[1] + ch4_ya[1]) # min of AAAB, BBBA
    s1_s3 = min(ch1_ya[1] + ch2_ya[1] + ch3_yb[1] + ch4_ya[1], ch1_yb[1] + ch2_yb[1] + ch3_ya[1] + ch4_yb[1]) # min of AABA, BBAB
    s1_s5 = min(ch1_ya[1] + ch2_yb[1] + ch3_yb[1] + ch4_yb[1], ch1_yb[1] + ch2_ya[1] + ch3_ya[1] + ch4_ya[1]) # min of ABBB, BAAA
    s1_s7 = min(ch1_ya[1] + ch2_yb[1] + ch3_ya[1] + ch4_ya[1], ch1_yb[1] + ch2_ya[1] + ch3_yb[1] + ch4_yb[1]) # min of ABAA, BABB

    s3_s1 = min(ch1_ya[3] + ch2_ya[3] + ch3_ya[3] + ch4_yb[3], ch1_yb[3] + ch2_yb[3] + ch3_yb[3] + ch4_ya[3]) # min of AAAB, BBBA
    s3_s3 = min(ch1_ya[3] + ch2_ya[3] + ch3_yb[3] + ch4_yb[3], ch1_yb[3] + ch2_yb[3] + ch3_ya[3] + ch4_ya[3]) # min of AABA, BBAB
    s3_s5 = min(ch1_ya[3] + ch2_yb[3] + ch3_yb[3] + ch4_yb[3], ch1_yb[3] + ch2_ya[3] + ch3_ya[3] + ch4_ya[3]) # min of ABBB, BAAA
    s3_s7 = min(ch1_ya[3] + ch2_yb[3] + ch3_ya[3] + ch4_ya[3], ch1_yb[3] + ch2_ya[3] + ch3_yb[3] + ch4_yb[3]) # min of ABAA, BABB

    s5_s1 = min(ch1_ya[5] + ch2_ya[5] + ch3_ya[5] + ch4_yb[5], ch1_yb[5] + ch2_yb[5] + ch3_yb[5] + ch4_ya[5]) # min of AAAB, BBBA
    s5_s3 = min(ch1_ya[5] + ch2_ya[5] + ch3_yb[5] + ch4_yb[5], ch1_yb[5] + ch2_yb[5] + ch3_ya[5] + ch4_ya[5]) # min of AABA, BBAB
    s5_s5 = min(ch1_ya[5] + ch2_yb[5] + ch3_yb[5] + ch4_yb[5], ch1_yb[5] + ch2_ya[5] + ch3_ya[5] + ch4_ya[5]) # min of ABBB, BAAA
    s5_s7 = min(ch1_ya[5] + ch2_yb[5] + ch3_ya[5] + ch4_ya[5], ch1_yb[5] + ch2_ya[5] + ch3_yb[5] + ch4_yb[5]) # min of ABAA, BABB

    s7_s1 = min(ch1_ya[7] + ch2_ya[7] + ch3_ya[7] + ch4_yb[7], ch1_yb[7] + ch2_yb[7] + ch3_yb[7] + ch4_ya[7]) # min of AAAB, BBBA
    s7_s3 = min(ch1_ya[7] + ch2_ya[7] + ch3_yb[7] + ch4_yb[7], ch1_yb[7] + ch2_yb[7] + ch3_ya[7] + ch4_ya[7]) # min of AABA, BBAB
    s7_s5 = min(ch1_ya[7] + ch2_yb[7] + ch3_yb[7] + ch4_yb[7], ch1_yb[7] + ch2_ya[7] + ch3_ya[7] + ch4_ya[7]) # min of ABBB, BAAA
    s7_s7 = min(ch1_ya[7] + ch2_yb[7] + ch3_ya[7] + ch4_ya[7], ch1_yb[7] + ch2_ya[7] + ch3_yb[7] + ch4_yb[7]) # min of ABAA, BABB
    




# Create a test signal
a = np.array([0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0,-1, 2, -2, 1, 0, 1, 2, -1, -2, 0, 1, 2, 0, -1, 2, 1, -1, 2, -2, 1, 0, 1, 2, -1, -2, 0, 1, 2, 0, -1, 2, 1])

# Create channel coefficients
f = np.array([1, 0.9, 0.4, 0.2, -0.2, -0.3, -0.5, -0.3, -0.2, -0.15, -0.11, -0.08, -0.06, -0.05, -0.045])

# Create channel noise samples
sigma = 0.1
n = a.size + f.size - 1
w = np.random.normal(0, sigma, n)

# Create received signal
r = np.convolve(a, f, 'full') + w

# run dfp algorithm
y= dfp(r, f[1:])

# Plot it
t = np.arange(0, y.size, 1)
plt.stem(t, y)
plt.title("Impulse Response")
plt.xlabel("Time")
plt.ylabel("Amplitude")
plt.grid(True)
plt.show()

t = np.arange(0, r.size, 1)
plt.stem(t, r)
plt.title("Impulse Response")
plt.xlabel("Time")
plt.ylabel("Amplitude")
plt.grid(True)
plt.show()


print(muxu(np.array([6, 7, 8, 9, 10]), np.array([0, 1, 2, 3, 4, 5]), np.array([-2, -2, 0, 0, 1, -1, 1, 1])))