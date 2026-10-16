evidence = np.array([0.5, 0.5])
coin1 = np.array([0.5, 0.5])
x6 = np.take(np.array([0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0]).reshape((2,2,2)), [1], axis=-1)
x7 = np.array([0.5, 0.5, 0.5, 0.5, 1.0, 0.0, 0.0, 1.0]).reshape((2,2,2))
normalized8 = np.einsum('a,b,abc,abd->d', evidence, coin1, x6, x7)
res9 = normalized8 / np.sum(normalized8)
res9
