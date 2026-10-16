firstCoin = np.array([0.5, 0.5])
secondCoin = np.array([0.5, 0.5])
bothHeads = np.array([1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0]).reshape((2,2,2))
x13 = np.take(np.array([0.0, 1.0, 1.0, 0.0]).reshape((2,2)), [1], axis=-1)
normalized14 = np.einsum('a,b,abc,cd->a', firstCoin, secondCoin, bothHeads, x13)
res15 = normalized14 / np.sum(normalized14)
res15
