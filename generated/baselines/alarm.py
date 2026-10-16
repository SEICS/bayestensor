earthquake = np.array([0.9999, 1.0E-4])
burglary = np.array([0.999, 0.001])
alarm = np.array([1.0, 0.0, 0.0, 1.0, 0.0, 1.0, 0.0, 1.0]).reshape((2,2,2))
phoneWorking = np.array([0.010000000000000009, 0.99, 0.30000000000000004, 0.7]).reshape((2,2))
maryWakes = np.array([0.8, 0.2, 0.8, 0.2, 0.4, 0.6, 0.19999999999999996, 0.8]).reshape((2,2,2))
called = np.take(np.array([1.0, 0.0, 1.0, 0.0, 1.0, 0.0, 0.0, 1.0]).reshape((2,2,2)), [1], axis=-1)
normalized1 = np.einsum('a,b,abc,ad,cae,edf->b', earthquake, burglary, alarm, phoneWorking, maryWakes, called)
res2 = normalized1 / np.sum(normalized1)
res2
