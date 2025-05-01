import random

# PAM-5 encoded symbols
# pam5_syms = [-8, -4, 0, 4, 8]
pam5_syms = [-2, -1, 0, 1, 2]
pam5_refs = [-2, -1, 0, 1, 2]

pam5_syms_A = [-52, 51]
pam5_syms_B = [-103, 0, 101]
pam5_refs_A = [-1, 1]
pam5_refs_B = [-2, 0, 2]

# Postcursor taps
postcursor_taps = [1, 0.2, -0.12, 0.06, -0.03, 0.15, -0.008, 0.008, 0.004, -0.002, 0, 0, 0, 0, 0]
#postcursor_taps = [1, 0, 0, 0, 0, 0, 0, 0,0, 0, 0, 0, 0, 0, 0]


def switch_state(state, sequence):
    if state == 0:
        if sequence == 'AAAA' or sequence == 'BBBB':
            return 0
        elif sequence == 'AABB' or sequence == 'BBAA':
            return 1
        elif sequence == 'ABBA' or sequence == 'BAAB':
            return 2
        elif sequence == 'ABAB' or sequence == 'BABA':
            return 3
        else:
            raise ValueError("Invalid state")
    elif state == 1:
        if sequence == 'AAAB' or sequence == 'BBBA':
            return 4
        elif sequence == 'AABA' or sequence == 'BBAB':
            return 5
        elif sequence == 'ABBB' or sequence == 'BAAA':
            return 6
        elif sequence == 'ABAA' or sequence == 'BABB':
            return 7
        else:
            raise ValueError("Invalid state")
    elif state == 2:
        if sequence == 'AAAA' or sequence == 'BBBB':
            return 1
        elif sequence == 'AABB' or sequence == 'BBAA':
            return 0
        elif sequence == 'ABBA' or sequence == 'BAAB':
            return 3
        elif sequence == 'ABAB' or sequence == 'BABA':
            return 2
        else:
            raise ValueError("Invalid state")
    elif state == 3:
        if sequence == 'AAAB' or sequence == 'BBBA':
            return 5
        elif sequence == 'AABA' or sequence == 'BBAB':
            return 4
        elif sequence == 'ABBB' or sequence == 'BAAA':
            return 7
        elif sequence == 'ABAA' or sequence == 'BABB':
            return 6
        else:
            raise ValueError("Invalid state")
    elif state == 4:
        if sequence == 'AAAA' or sequence == 'BBBB':
            return 2
        elif sequence == 'AABB' or sequence == 'BBAA':
            return 3
        elif sequence == 'ABBA' or sequence == 'BAAB':
            return 0
        elif sequence == 'ABAB' or sequence == 'BABA':
            return 1
        else:
            raise ValueError("Invalid state")
    elif state == 5:
        if sequence == 'AAAB' or sequence == 'BBBA':
            return 6
        elif sequence == 'AABA' or sequence == 'BBAB':
            return 7
        elif sequence == 'ABBB' or sequence == 'BAAA':
            return 4
        elif sequence == 'ABAA' or sequence == 'BABB':
            return 5
        else:
            raise ValueError("Invalid state")
    elif state == 6:
        if sequence == 'AAAA' or sequence == 'BBBB':
            return 3
        elif sequence == 'AABB' or sequence == 'BBAA':
            return 2
        elif sequence == 'ABBA' or sequence == 'BAAB':
            return 1
        elif sequence == 'ABAB' or sequence == 'BABA':
            return 0
        else:
            raise ValueError("Invalid state")
    elif state == 7:
        if sequence == 'AAAB' or sequence == 'BBBA':
            return 7
        elif sequence == 'AABA' or sequence == 'BBAB':
            return 6
        elif sequence == 'ABBB' or sequence == 'BAAA':
            return 5
        elif sequence == 'ABAA' or sequence == 'BABB':
            return 4
        else:
            raise ValueError("Invalid state")
    else:
        raise ValueError("Invalid state")

def generate_sequence(state):
    sequence = ''
    if state % 2 == 0:
        sequence += random.choice(['AAAA', 'BBBB', 'ABAB', 'BABA', 'ABBA', 'BAAB', 'AABB', 'BBAA'])
    else:
        sequence += random.choice(['AAAB', 'BBBA', 'AABA', 'BBAB', 'ABBB', 'BAAA', 'ABAA', 'BABB'])
    return sequence

def generate_symbols(sequence):
    ref_syms = []
    test_syms = []
    for i in range(len(sequence)):
        if sequence[i] == 'A':
            idx_A = random.randint(0, 1)
            ref_syms.append(pam5_refs_A[idx_A])
            test_syms.append(pam5_syms_A[idx_A])
        else:
            idx_B = random.randint(0, 2)
            ref_syms.append(pam5_refs_B[idx_B])
            test_syms.append(pam5_syms_B[idx_B])
    return ref_syms, test_syms

state = 0
seq = ''
channels_syms = [[] for _ in range(4)]
channels_refs = [[] for _ in range(4)]

for i in range(100):
    seq = generate_sequence(state)
    state = switch_state(state, seq)
    ref_syms, test_syms = generate_symbols(seq)  # returns list of 4 values: [A, B, C, D]
    for ch in range(4):
        channels_syms[ch].append(test_syms[ch])
        channels_refs[ch].append(ref_syms[ch])


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

#Save the reference symbols
with open("ref_vectors.txt", "w") as f_ref:
    for n in range(100):
        refs = [str(channels_refs[ch][n]) for ch in range(4)]
        f_ref.write(" ".join(refs) + "\n")

print("Generated test_vectors.txt and ref_vectors.txt")

# Save the postcursor taps (ignoring the first tap)
with open("tap_vector.txt", "w") as f_tap:
    taps_to_write = postcursor_taps[1:]  # Ignore the first tap
    scaled_taps = [int(round(tap * 128)) for tap in taps_to_write]
    f_tap.write(" ".join(str(tap) for tap in scaled_taps))