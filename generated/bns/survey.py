A = np.array([0.3, 0.5, 0.2])
S = np.array([0.6, 0.4])
E = np.einsum('a,b,abc->c', A, S, np.array([0.75, 0.25, 0.64, 0.36, 0.72, 0.28, 0.7, 0.3, 0.88, 0.12, 0.9, 0.1]).reshape((3,2,2)))
R = np.einsum('a,ab->b', E, np.array([0.25, 0.75, 0.2, 0.8]).reshape((2,2)))
O = np.einsum('a,ab->b', E, np.array([0.96, 0.04, 0.92, 0.08]).reshape((2,2)))
T = np.einsum('a,b,abc->c', O, R, np.array([0.48, 0.42, 0.1, 0.58, 0.24, 0.18, 0.56, 0.36, 0.08, 0.7, 0.21, 0.09]).reshape((2,2,3)))
T
