BayesTensor
===

BayesTensor is a tensor-based probabilistic programming framework for supporting fast exact inference over discrete distributions.

## Perquisite
- Python 3.8+ (managed via Conda)
- Scala 2.12 (for running the compiler/driver)
- [sbt](https://docs.scala-lang.org/getting-started/sbt-track/getting-started-with-scala-and-sbt-on-the-command-line.html)
- [hyperfine](https://github.com/sharkdp/hyperfine)

## Installation

1.  **Clone the repository:**
    ```bash
    git clone git@github.com:SEICS/bayestensor.git
    cd bayestensor
    ```

2.  **Set up the Python environment:**
    ```bash
    conda env create -f py312.yml
    conda activate py312
    ```

## Running Experiments & Reproducing Results
Ahead-of-time compilation (needed to run the bash script for evaluation):
```bash
sbt bayestensor/nativeImage
```

Script for evaluation:
```bash
sh scripts/bench_e2e.sh
```

To generate bn and baseline examples:
```bash
sbt run
```
Or you can invoke sbt console and run programs inside the sbt. Successful compilation of that should show:
```bash
jw@MacBook-Pro-327 bayestensor % sbt
[info] welcome to sbt 1.6.0 (Eclipse Adoptium Java 11.0.28)
[info] loading settings for project bayestensor-build from plugins.sbt ...
[info] loading project definition from /Users/jw/Desktop/bayestensor/project
[info] loading settings for project bayestensor from build.sbt ...
[info] set current project to bayestensor (in build file:/Users/jw/Desktop/bayestensor/)
[info] 
[info] Here are some highlights of this release:
[info]   - Improved JDK 17 support
[info]   - Improved Build Server Protocol (BSP) support
[info]   - Tab completion of global keys
[info] See https://eed3si9n.com/sbt-1.6.0 for full release notes.
[info] Hide the banner for this release by running `skipBanner`.
[info] sbt server started at local:///Users/jw/.sbt/1.0/server/d5be383a3b120cbba0c5/sock
[info] started sbt server
sbt:bayestensor> 
```

Then, run a bn example in sbt console:
```bash
run cancer bn
```

Run a baseline example in sbt console:
```bash
run alarm baseline
```

If you find BayesTensor useful, please cite us in your work:
```bash

@inproceedings{10.1145/3578360.3580258,
author = {Pan, Jingwen and Shaikhha, Amir},
title = {Compiling Discrete Probabilistic Programs for Vectorized Exact Inference},
year = {2023},
url = {https://doi.org/10.1145/3578360.3580258},
booktitle = {Proceedings of the 32nd ACM SIGPLAN International Conference on Compiler Construction}
}
```

