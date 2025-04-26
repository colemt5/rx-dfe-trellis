import random

# PAM-5 encoded symbols
pam5_symbols = [-16, -8, 0, 8, 16]

# Postcursor taps
postcursor_taps = [-3.2, 2.4, -0.192, 1.44, -1.12, 0.8, -0.64, 0.48, -0.32, 0.32, -0.16, 0.16, 0.0, 0.0]

# Load the 100x4 symbols
input_symbols = []

# First generate the random symbols (as an example)
for _ in range(100):
    line = [random.choice(pam5_symbols) for _ in range(4)]
    input_symbols.append(line)

# Separate symbols into 4 channels
channels = [[] for _ in range(4)]
for line in input_symbols:
    for i in range(4):
        channels[i].append(line[i])

# Convolve each channel with the postcursor taps
convolved_channels = [[] for _ in range(4)]

for ch in range(4):
    stream = channels[ch]
    for n in range(len(stream)):
        acc = 0.0
        for k in range(len(postcursor_taps)):
            if n - k >= 0:
                acc += stream[n - k] * postcursor_taps[k]
        convolved_channels[ch].append(acc)  # store raw float

# Save the output: 100 lines, each with 4 convolved values (rounded here)
with open("test_vectors.txt", "w") as f:
    for n in range(100):
        outputs = [str(int(round(convolved_channels[ch][n]))) for ch in range(4)]
        f.write(" ".join(outputs) + "\n")

print("Generated test_vectors.txt")