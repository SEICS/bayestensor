Pollution = np.array([0.5, 0.4, 0.1])
Smoker = np.array([0.3, 0.7])
Cancer = np.einsum('a,b,abc->c', Pollution, Smoker, np.array([0.03, 0.97, 0.001, 0.999, 0.03, 0.97, 0.001, 0.999, 0.05, 0.95, 0.02, 0.98]).reshape((3,2,2)))
Dyspnoea = np.einsum('a,ab->b', Cancer, np.array([0.65, 0.35, 0.3, 0.7]).reshape((2,2)))
Xray = np.einsum('a,ab->b', Cancer, np.array([0.9, 0.1, 0.2, 0.8]).reshape((2,2)))
Xray
