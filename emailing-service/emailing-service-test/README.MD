# Testing in c#

---
So testing in c# is a bit different than in Java, you will have to run the following commands in order to get your test coverage.
```
dotnet test --collect:"XPlat Code Coverage"
```
This will generate a result in the TestResult folder. Sadly, it is unreadable, so you will have to do the following in order to read it!
First, install this library so that you can transform that file into a html file. You will only need to do this once
```
dotnet tool install -g dotnet-reportgenerator-globaltool
```
From now on, still in the root folder of the test project (not the solution), enter :
```
reportgenerator "-reports:TestResults/**/*.xml" "-targetdir:coveragereport"

```

