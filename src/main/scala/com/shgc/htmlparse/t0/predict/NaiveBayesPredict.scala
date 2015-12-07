//    val sc = SparkManagerFactor.getSparkContext("g")
//    val model = NaiveBayesModel.load(sc, model)
////    val conf = SparkManagerFactor.getHBaseConf()
////    val connection = ConnectionFactory.createConnection(conf)
////    val table = connection.getTable(TableName.valueOf("qy58"))
////    val scan = new Scan()
////    for(column <- columns) scan.addColumn(columnFamily, column)
////    val scanner = table.getScanner(scan)
//
//
//    val urls = rdd.map{case(url, value) =>{(url, model.predict(value))}}.filter(_._2 > 0.0).map(_._1.toString).collect()
////    val result = r.map()
//    val result = getFullInformation(urls)
//    sc.parallelize(result)