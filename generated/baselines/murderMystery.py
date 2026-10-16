aliceDunnit = np.array([0.7, 0.3])
withGun = np.array([0.19999999999999996, 0.8, 0.97, 0.03]).reshape((2,2))
obs = np.take(np.array([1.0, 0.0, 0.0, 1.0]).reshape((2,2)), [1], axis=-1)
normalized17 = np.einsum('a,ab,bc->a', aliceDunnit, withGun, obs)
res18 = normalized17 / np.sum(normalized17)
res18
