evidence = np.array([0.5, 0.5])
coin = np.array([0.5, 0.5])
x3 = np.take(np.array([0.0, 1.0, 0.0, 1.0, 1.0, 0.0, 0.0, 1.0]).reshape((2,2,2)), [1], axis=-1)
normalized4 = np.einsum('a,b,abc->a', evidence, coin, x3)
res5 = normalized4 / np.sum(normalized4)
res5
