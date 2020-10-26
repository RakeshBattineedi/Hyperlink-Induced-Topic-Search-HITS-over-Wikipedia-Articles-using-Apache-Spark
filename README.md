# Hyperlink-Induced-Topic-Search-HITS-over-Wikipedia-Articles-using-Apache-Spark

The goal of this project is to gain experience in:

• Installing and using analytics tools such as HDFS and Apache Spark

• Generating root set and base set based on link information

• Implementing iterative algorithms to estimate Hub and Authority scores of Wikipedia articles using Wikipedia dump data

1. Overview

In this project, you will design and implement a system that generates a subset of graphs using Hyperlink-Induced Topic Search (HITS)12 over currently available Wikipedia
articles3. HITS is a link analysis algorithm designed to find the key pages for specific web communities. HITS is also known as hubs and authorities. HITS relies on two values associated with each page: one is called its hub score and the other its authority score. For any query, you compute two ranked lists of results. The ranking of one list is induced by the hub scores and that of the other by the authority scores. This approach stems from a particular insight into the very early creation of web pages --- that there are two primary kinds of web pages that are useful as results for broad-topic searches. For example, consider an information query such as “I wish to learn about the eclipse”. There are authoritative sources of information on the topic; in this case, the National Aeronautics and Space Administration (NASA)’s page on eclipse would be such a page. We will call such pages as authorities. In contrast, there are pages containing lists of links (hand-compiled) to authoritative web pages on a particular topic. These hub pages are not in themselves authoritative sources of topic but they provide aggregated information. We use these hub pages to discover the authority pages.


Dataset

The original dump dataset from Wikipedia is 12.1GB as a bz2 file, and about 50GB when it is decompressed. It follows an XML format and includes other information that is not relevant to this task. To optimize your work on this assignment, we will use Wikipedia dump4 generated by Henry Haselgrove. 
The dataset contains two files:
links-simple-sorted.zip (323MB)
titles-sorted.zip (28MB)

These files contain all links between proper Wikipedia pages. This includes disambiguation pages and redirect pages. It includes only the English language Wikipedia.
In links-simple-sorted.txt, there is one line for each page that has links from it. The
format of the lines is:
from1: to11 to12 to13 ...
from2: to21 to22 to23 ...
...

where from1 is an integer labeling a page that has links from it, and to11 to12 to13 ... are integers labeling all the pages that the page links to. To find the page title that corresponds to integer n, just look up the n-th line in the file titles-sorted.txt, a UTF-8 encoded text file.
