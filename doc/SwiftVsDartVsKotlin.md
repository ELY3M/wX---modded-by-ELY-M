
**Data Types**
```
dart: String int double bool (final)
swift: String Int Double Bool (let)
kotlin: String Int Double Boolean (val)
```

**Function signature**
```
dart: String funName(String a) {}
swift: func funName(a: String) -> String {}
kotlin: fun funName(a: String): String {}
```

**Parse a string and return a double with error checking**
```
Dart:
lon = double.tryParse(string.substring(4, 6) + "." + string.substring(6, 7)) ??  0.0;

Swift:
let tempD = Double(value) ?? 0.0

Kotlin:
y = newValue.toDoubleOrNull() ?: 0.0
```

removeAt
remove(at:)

**Function signature:**

```
Dart:
static List<double> parseLatLon(String string) {

Swift:
static func parseLatLon(_ string: String) -> [Double] {

Kotlin:
fun addColdFrontTriangles(front: Fronts, tokens: List<String>) {
```


**Char at index in String**
```
Swift:
let index = string.index(string.startIndex, offsetBy: 3)
String(string[index])  
``` 

**for loop***
```
Dart:
for (var file in files) {}

Swift:
for file in files {}

Kotin:
for (file in files) {}
```

**for loop index iterate**
```
Dart:
for (int index = 0; index < tokens.length; index += 1) {
UtilityWpcFronts.pressureCenters.asMap().forEach((index, value) {


Swift:
warningDataList.enumerated().forEach { index, warningData in
for index in stride(from: 0, to: tokens.count, by: 2) {

Kotlin:
tokens.indices.forEach { index ->
```

Kotlin:
(100 downTo -1 step 1).forEach {
(0 until 256).forEach { // does not include 256
(0..256).forEach {      // DOES include 256
for (index in startIndex until tokens.size step indexIncrement) {
for (index in startIndex..tokens.size step indexIncrement) {

Swift - modify list passed as arg to method (inout and &)
```
func showTextWarnings(_ views: inout [UIView]) {
self.showTextWarnings(&views)
```

**List Size**
```
Dart:
length

swift:
count
```


**List add to**
```
Dart:
add

Kotlin:
add

Swift:
append
```


Floor
```
let numberOfTriangles = (distance / length).floor()

var x = 6.5
x.round(.towardZero)

Kotlin:
import kotlin.math.floor
val numberOfTriangles = floor(distance / length)
```


**Math - PI**
```
dart:
math.pi

Swift:
Double.pi

Kotlin:
import kotlin.math.*
PI
```


**Long data type**
```
Swift: let varName:CLong = 0
Kotlin var varName = 0.toLong()
```

**List initialization**
```
Kotlin: mutableListOf("")     MutableList<String>
Swift [String]()       [String]
Dart <String>[]      List<String>
```

**forEach loop with both index and value**
```
Kotlin:
list.forEachIndexed { index, value ->

Dart:
list.asMap().forEach((index, value) => f);

Swift:
list.enumerated().forEach { index, value in
```

**Dictionary**
```
kotlin:
val classToId: MutableMap<String, String> = mutableMapOf()
val inferMap = {"a":"b", "c":"d"}

swift:
var classToId: [String: String] = [:]
let inferMap = ["a":"b", "c":"d"]

dart:
var classToId = Map<int, List<double>>();
```

**Iterate over enum:**
```
kotlin:
NhcOceanEnum.values().forEach {}

String representation:
NhcOceanEnum.ATL.name

Dart:
NhcOceanEnum.values.forEach ((data){});

Swift:

enum NhcOceanEnum: CaseIterable {

NhcOceanEnum.allCases.forEach {
   regionMap[$0] = ObjectNhcRegionSummary($0)
}
```

**Passing functions**
```
kotlin:
var functions: List<(Int) -> Unit>
bottomSheetFragment.functions = listOf(::edit, ::delete, ::moveUp, ::moveDown)
    fun setListener(context: Context, drw: ObjectNavDrawer, fn: () -> Unit) {
    img.setListener(this, drw, ::getContentFixThis)

swift:
var renderFn: ((Int) -> Void)?
self.renderFn!(paneNumber)

func setRenderFunction(_ fn: @escaping (Int) -> Void) {
        self.renderFn = fn
}

dart:
Function(int) fn

```

TODOS: default variables in methods
TODOS: class initializers
TODOS: enum
TODOS: extensions
