import random
import math
import numpy as np

# postcursor_taps = [1, 0.15, -0.04, 0.03, -0.002, 0.001, -0.008, 0.008, 0.004, 0, 0, 0, 0, 0, 0]
postcursor_taps = [1, 0.4, 0.2, 0.1, 0.05, 0.03, 0.01, 0.009, 0.008, 0.006, 0.0001, 0, 0, 0, 0]
noise_std_dev = 0.10  # adjust for desired SNR

def read_encoder_vectors(filename="ref_vectors.txt"):
    data = np.loadtxt(filename, dtype=int)
    if data.shape[1] != 4:
        raise ValueError(f"Expected 4 values per line, got {data.shape[1]}")
    channels_refs = data.T
    return channels_refs

channels_refs = read_encoder_vectors()

channels_syms = np.zeros_like(channels_refs, dtype=float)
channels_noise = np.zeros_like(channels_refs, dtype=float)

# convolve with channel impulse response and add noise
for ch in range(4):
    channels_syms[ch] = np.convolve(channels_refs[ch], postcursor_taps, mode='full')[:len(channels_refs[ch])]
    channels_noise[ch] = np.random.normal(0, noise_std_dev, size=len(channels_refs[ch]))
    channels_syms[ch] += channels_noise[ch]

# Compute and display SNR before scaling
total_signal_power = np.mean(channels_refs**2)
total_noise_power = np.mean((channels_syms-channels_refs)**2)
snr = 10 * np.log10(total_signal_power / total_noise_power)
print("------")
print(f"SNR before scaling: {snr:.2f} dB")
print("------")

max_val = np.max(channels_syms)
min_val = np.min(channels_syms)
abs_max = max(abs(max_val), abs(min_val))
if abs_max > 0:
    scale_factor = 127 / abs_max
    print (f"PAM5 Level: {scale_factor}")
    channels_syms *= scale_factor

# Save the convolved output
with open("test_vectors.txt", "w") as f_conv:
    for n in range(channels_syms.shape[1]):
        outputs = [str(int(round(channels_syms[ch][n]))) for ch in range(4)]
        f_conv.write(" ".join(outputs) + "\n")

# Save the postcursor taps (ignoring the first tap)
with open("tap_vector.txt", "w") as f_tap:
    taps_to_write = postcursor_taps[1:]  # Ignore the first tap
    scaled_taps = [int(round(tap * 128)) for tap in taps_to_write]
    f_tap.write(" ".join(str(tap) for tap in scaled_taps))

print("Generated test_vectores.txt, ref_vectors.txt, and tap_vector.txt")