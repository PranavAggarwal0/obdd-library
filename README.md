# Instructions for Using the OBDD Library + Frontend App

## Using the Library

The files in `y3-project-obdd-lib` constitute the library developed for the project.  
The library can be used for developing Scala applications that require management of OBDDs.  
To use the library in your project, follow the instructions below. Note that you will need to have [sbt](https://www.scala-sbt.org) installed on the development machine.

```bash
cd y3-project-obdd-lib
```

```bash
sbt clean compile publishLocal
```

The library is now published on your machine. This means that it is ready to be included in other projects. An example is presented below:

```scala
import obdd.api.OBDDLib.*

@main def hello: Unit = {
  def ctx     = newOBDD(Seq("x1", "x2", "x3", "x4"))
  val (c1, n1) = getNodeByExpression("!x2").run(ctx).value
  for {
    node1 <- n1
    (c2, n2)    = negation(node1).run(c1).value
    node2 <- n2
  } yield (println(visualiseDiagram(getDiagramByNode(node2, c2))))
}
```

## Using the Frontend

As part of the project, a frontend application was developed to visualise the methods provided by the library's API.  
To use the frontend, follow the below instructions:

```bash
cd y3-project-obdd-lib
sbt clean compile publishLocal
```

```bash
cd ../y3-project-obdd-app
sbt run
```

```bash
cd ../y3-project-obdd-app-frontend
npm i
npm run dev
```

<img width="655" alt="Screenshot 2025-07-08 at 12 58 06" src="https://github.com/user-attachments/assets/4588c191-915a-4bec-a139-b307661c608b" />

<img width="533" alt="Screenshot 2025-07-08 at 12 58 28" src="https://github.com/user-attachments/assets/ae930f4d-944c-44dd-ba39-981fae3bbabf" />


