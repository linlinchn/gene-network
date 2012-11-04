README
======

Run without arguments or --help to display usage:

	>> java -jar Evaluation.jar --help

1. COMPUTING PRECISION-RECALL AND ROC CURVES
--------------------------------------------

To compute PR and ROC curves, as well as the area under the curves (AUPR and AUROC), specify a prediction file and a gold standard file in DREAM5 format:

	>> java -jar Evaluation.jar --pred <file> --gold <file>
	
To compute only the PR / ROC curve, use the option --PR / --ROC.

2. ANALYZING SYSTEMATIC PREDICTION ERRORS
-----------------------------------------

Use the option --motif to analyze systematic prediction errors, namely transitive edges and co-regulation edges:

	>> java -jar Evaluation.jar --pred ecoli_GENIE3.txt --gold ecoli_regulondb.txt --motif
	
IMPORTANT: You first have to apply a cutoff, the given network is interpreted as a binary network prediction, where all listed edges are considered present independently of their score/rank in the list.

The expected number of transitive/co-regulation in an unbiased prediction (randomized prediction with the same number of true positives and false positives) is also computed. This is simply the fraction of transitive/co-regulation edges in the gold standard multiplied by the number of false positives in the supplied prediction.

The observed number of transitive/co-regulation is typically higher than the expected number of transitive/co-regulation edges.
