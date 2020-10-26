import org.apache.spark.sql.SparkSession


object hits {

  def main(args: Array[String]) {

    val sc = SparkSession.builder().appName("hits")
      .master("local")
      .getOrCreate();

    val links = sc.read.textFile(args(0)).rdd
    val sc1 = sc.sparkContext

    val titlesfile = sc.read.textFile(args(1)).rdd.zipWithIndex().mapValues(x => x + 1).map(_.swap);
    val titlefinal = titlesfile.map { case (index, name) => (index.toString, name) }
    val word = args(2)
    val linksfile = links.filter(s1 => !s1.endsWith(": ")).map(s => (s.split(": ")(0), s.split(": ")(1).split(" "))).flatMapValues(x => x).map { case (k, v) => (k, v) }
    val rootset = titlefinal.filter { case (k, v) => v.contains(word) }

    val dummy = rootset.keys
    val tolinks = linksfile.join(rootset).map { case (k, (v1, v2)) => (k, v1) }.map(_.swap).groupByKey().keys.union(dummy).distinct()
    val flappedlinkfile = linksfile.map(_.swap)
    val baseset = flappedlinkfile.join(rootset).map { case (k, (v1, v2)) => (k, v1) }.map(_.swap).groupByKey().keys.union(tolinks).distinct()

    // first iteration for auth
    val filtauthlinks = baseset.map(k => (k, 1)).join(flappedlinkfile)
    var auth = filtauthlinks.map { case (k, (v1, v2)) => (k, v1.toDouble) }.reduceByKey(_ + _)
    var sumauth = auth.map { case (k, v) => v }.sum
    auth = auth.map { case (k, v) => (k, v / sumauth) }
    var authmap = auth.collectAsMap()

    // first iteration for hub
    val filthublinks = baseset.map(k => (k, 1)).join(linksfile).map { case (k, (v1, v2)) => (k, v2) }
    var hub = filthublinks.map { case (k, v) => (k, authmap.get(v)) }.map { case (k, Some(v)) => (k, v); case (k, None) => (k, 0.0) }.reduceByKey(_ + _)
    var sumHub = auth.map { case (k, v) => v }.sum
    hub = hub.map { case (k, v) => (k, v / sumHub) }
    var hubMap = hub.collectAsMap()

    for (w <- 0 to 25) {
      auth = filtauthlinks.map { case (k, (v1, v2)) => (k, v2) }.map { case (k, v) => (k, hubMap.get(v)) }.map { case (k, Some(v)) => (k, v); case (k, None) => (k, 0.0) }.reduceByKey(_ + _)
      sumauth = auth.map { case (k, v) => v }.sum
      auth = auth.map { case (k, v) => (k, v / sumauth) }
      authmap = auth.collectAsMap()

      hub = filthublinks.map { case (k, v) => (k, authmap.get(v)) }.map { case (k, Some(v)) => (k, v); case (k, None) => (k, 0.0) }.reduceByKey(_ + _)
      sumHub = hub.map { case (k, v) => v }.sum
      hub = hub.map { case (k, v) => (k, v / sumauth) }
      hubMap = hub.collectAsMap()
    }

    //val authsorted = baseset.map { case(k) => (k, authmap.get(k))}.sortBy(_._2, false)
    val authsorted = baseset.map { case (k) => (k, authmap.get(k)) }.join(titlefinal).sortBy(_._2, false).collect()
    val hubsorted = baseset.map { case (k) => (k, hubMap.get(k)) }.join(titlefinal).sortBy(_._2, false).collect()
    sc1.parallelize(authsorted.toSeq).coalesce(1, shuffle = true).sortBy(_._2, false).saveAsTextFile(args(3))
    sc1.parallelize(hubsorted.toSeq).coalesce(1, shuffle = true).sortBy(_._2, false).saveAsTextFile(args(4))

  }

}
