import random

# PAM-5 encoded symbols
# pam5_syms = [-8, -4, 0, 4, 8]
pam5_syms = [-103, -52, 0, 51, 101]
pam5_refs = [-2, -1, 0, 1, 2]

# Postcursor taps
# postcursor_taps = [4, -1.6, 1.2, -0.096, 0.72, -0.56, 0.4, -0.32, 0.24, -0.16, 0.16, -0.08, 0.08, 0.0, 0.0]
postcursor_taps = [1, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0]

# Initialize symbol and reference lists
channels_syms = [[] for _ in range(4)]
channels_refs = [[] for _ in range(4)]

# Generate random symbols
for _ in range(100):
    for ch in range(4):
        idx = random.randint(0, 4)
        channels_syms[ch].append(pam5_syms[idx])
        channels_refs[ch].append(pam5_refs[idx])

# Convolve each channel with the postcursor taps
channels_syms_conv = [[] for _ in range(4)]

for i in range(100):
    for ch in range(4):
        acc = 0.0
        for j in range(len(postcursor_taps)):
            if i - j >= 0:
                acc += channels_syms[ch][i - j] * postcursor_taps[j]
        channels_syms_conv[ch].append(acc)

# Save the convolved output
with open("test_vectors.txt", "w") as f_conv:
    for n in range(100):
        outputs = [str(int(round(channels_syms_conv[ch][n]))) for ch in range(4)]
        f_conv.write(" ".join(outputs) + "\n")

# Save the reference symbols
with open("ref_vectors.txt", "w") as f_ref:
    for n in range(100):
        refs = [str(channels_refs[ch][n]) for ch in range(4)]
        f_ref.write(" ".join(refs) + "\n")

print("Generated test_vectors.txt and ref_vectors.txt")

# Save the postcursor taps (ignoring the first tap)
with open("tap_vector.txt", "w") as f_tap:
    taps_to_write = postcursor_taps[1:]  # Ignore the first tap
    f_tap.write(" ".join(str(tap) for tap in taps_to_write))