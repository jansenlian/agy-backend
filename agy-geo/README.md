# AGY GIS 空间数据处理引擎与 CAD 解析类库 (agy-geo)

`agy-geo` 是 AGY 极速基座体系下的**专业 GIS 空间拓扑与 CAD 矢量图斑提取工具库**，支持将 AutoCAD `.dwg` 与 `.dxf` 工程图纸解析转换为 GIS 标准矢量要素 (GeoJSON / JTS Geometry / WKT)。

---

## 📂 模块架构与职责定位

```
                             ┌───────────────────────────────────┐
                             │              geo-all              │
                             │       (GIS 空间工具聚合包)        │
                             └───────────────┬───────────────────┘
                                             │
                       ┌─────────────────────┴─────────────────────┐
                       ▼                                           ▼
         ┌───────────────────────────┐               ┌───────────────────────────┐
         │         geo-core          │               │          geo-dwg          │
         │   (JTS 空间算法与拓扑引擎) │ ◄──────────── │ (LibreDWG 转码与 CAD 服务) │
         └───────────────────────────┘               └───────────────────────────┘
```

1. **`geo-core`（GIS 空间拓扑计算与解析核心引擎）**：
   * **JTS 自动拓扑构面引擎** (`JtsPolygonizerService`)：利用 JTS `Polygonizer` 将 CAD 中的离散线段、未闭合折线自动封闭成面；
   * **R-Tree 空间索引注记挂接** (`SpatialAttributeMatcher`)：采用 `STRtree` 建立空间几何索引，毫秒级完成 Point-in-Polygon 拓扑匹配，将地块文字注记自动挂接为图斑业务属性；
   * **DXF 组码流式解析引擎** (`DxfGroupCodeParser`)：高性能解析多段线 (`LWPOLYLINE`/`POLYLINE`)、线段 (`LINE`)、注记文字 (`TEXT`/`MTEXT`/`ATTRIB`)；
   * **GeoJSON 与 WKT 转换** (`GeoJsonUtil`)：输出符合 RFC 7946 规范的标准 GeoJSON FeatureCollection 及 OGC WKT；
   * **纯净性**：零 Web 容器、零自动装配，可在任何批处理、大数据或微服务中直接调用。

2. **`geo-dwg`（CAD 转码与流程编排类库）**：
   * **DWG/DXF 双格式兼容** (`LibreDwgConverter`)：安全调度底层 `dwg2dxf` 工具链进行高版本 DWG 转码；
   * **CAD 流程编排服务** (`CadService` / `CadServiceImpl`)：统一文件转码、图层过滤、构面与属性挂接全流程；
   * **纯净性**：作为纯 Jar 类库发布，不含内置 Web 容器与打包插件。

3. **`geo-all`（全量工具聚合包）**：
   * 一键聚合引入 `geo-core` 与 `geo-dwg`，后续可平滑扩展 `geo-shp`、`geo-gdb` 等模块。

---

## 🛠️ 外部依赖环境 (LibreDWG)

若需要处理 `.dwg` 二进制图纸，需在操作系统中安装 `dwg2dxf` 命令行工具（处理 `.dxf` 格式无需任何额外外部依赖）：

- **macOS**:
  ```bash
  brew install libredwg
  ```
- **Linux (Ubuntu/Debian)**:
  ```bash
  apt-get update && apt-get install -y libredwg-tools
  ```

---

## 💻 业务系统接入指南

在业务微服务（如 `agy-util`）中引入 `geo-all`：

```xml
<dependency>
    <groupId>com.greatmap.agy</groupId>
    <artifactId>geo-all</artifactId>
</dependency>
```

直接注入 `CadService` 即可使用 CAD 矢量化全部能力：

```java
@Autowired
private CadService cadService;

// 解析并获取 GeoJSON
JSONObject geoJson = cadService.parseToGeoJson(file, option);
```
