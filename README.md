# 用 java 掃描圖片輪廓

## 摘要

本專題以 Java 實作影像處理中的偏微分與邊緣檢測技術。程式透過一階差分核心計算影像的 x 方向與 y 方向梯度，並進行非線性增強與正規化，最終產生清晰的邊緣檢測結果。同時採用可調式參數，讓使用者能自由調整邊緣靈敏度與梯度增強強度。

## 研究目標

1. 以一階差分核心 `[-1, 1]` 估計影像偏微分，學習基礎梯度計算。
2. 分別產生 x 方向梯度圖 (dx)、y 方向梯度圖 (dy)，展示不同方向的邊緣特徵。
3. 透過非線性增強與亮度調整，提升梯度圖的視覺對比度，讓細節更明顯。
4. 結合梯度幅度計算邊緣偵測，提供可調式邊緣閾值供使用者篩選強邊緣。
5. 提供適合課堂實驗與報告展示的影像處理示範程式。

## 系統設計

本專案以單一 Java 程式組織，核心流程如下：

### 1. 灰階轉換 (`toGray()`)
- 讀取 RGB 圖片，使用亮度加權公式轉灰階：`value = 0.299R + 0.587G + 0.114B`
- 簡化後續梯度計算

### 2. 偏微分計算
- **x 方向梯度**：`gx = f(x+1, y) - f(x, y)`
- **y 方向梯度**：`gy = f(x, y+1) - f(x, y)`
- 各像素計算絕對值與邊緣幅度 `edge = √(gx² + gy²)`

### 3. 正規化與增強 (`normalizeToByte()` 與 `boostDerivative()`)
- 對整張圖進行全局正規化，根據最大梯度值映射至 `[0, 255]` 範圍
- 應用門檻值 (noise floor) 濾除微弱噪聲
- 使用 gamma 修正 (γ=0.75) 拉亮中低梯度，再進行倍率增強
- **增強公式**：`boosted = (normalized^0.75)^255 × boost% / 100`

### 4. 多輸出影像產生
- `output_dx.png`：增強後的 x 方向梯度
- `output_dy.png`：增強後的 y 方向梯度
- `output_edge.png`：二值邊緣圖（強邊緣為白色，弱邊緣為黑色）

## 操作流程

1. 準備輸入圖片 `image.png`（或指定其他路徑）
2. 執行程式，指定輸入與輸出檔案路徑，以及可選參數
3. 程式自動計算梯度、進行增強、產生三張輸出圖片
4. 檢視 `output_dx.png`、`output_dy.png`、`output_edge.png` 觀察不同方向的邊緣特徵

## 時間複雜度分析

令：
- `w`、`h` = 圖片寬高
- `n` = 像素總數 = `w × h`

主要步驟的複雜度如下：

### 灰階轉換
- 遍歷每個像素進行亮度加權轉換
- 複雜度：`O(w × h)`

### 偏微分與梯度計算
- 每個像素進行有限次乘除與比較運算
- 複雜度：`O(w × h)`

### 正規化與增強
- 再次遍歷每個像素，進行 gamma 修正與倍率計算
- 複雜度：`O(w × h)`

### 圖片輸出
- 逐像素寫入 RGB 值至 BufferedImage
- 複雜度：`O(w × h)`

**整體時間複雜度**：`O(w × h)`
**空間複雜度**：`O(w × h)`（儲存原始圖、梯度矩陣、輸出圖片）

## 程式檔案

1. `ImagePartialDerivatives.java`
   - 主程式，包含灰階轉換、梯度計算、增強與圖片輸出

2. `image.png`
   - 輸入圖片（可使用任意路徑替換）

3. `output_dx.png`
   - 程式輸出，x 方向梯度增強圖

4. `output_dy.png`
   - 程式輸出，y 方向梯度增強圖

5. `output_edge.png`
   - 程式輸出，二值邊緣檢測圖

## 執行方式

### 編譯
```bash
javac ImagePartialDerivatives.java
```

### 執行（使用預設參數）
```bash
java ImagePartialDerivatives
```
此時會讀取同資料夾的 `image.png`，輸出 `output_dx.png`、`output_dy.png`、`output_edge.png`。

### 執行（自訂參數）
```bash
java ImagePartialDerivatives <input_path> <dx_output> <dy_output> <edge_output> <edge_threshold> <derivative_boost%>
```

**參數說明：**
- `input_path`：輸入圖片路徑（預設：`image.png`）
- `dx_output`：dx 輸出路徑（預設：`output_dx.png`）
- `dy_output`：dy 輸出路徑（預設：`output_dy.png`）
- `edge_output`：邊緣輸出路徑（預設：`output_edge.png`）
- `edge_threshold`：邊緣二值化門檻，0～255（預設：`60`）
- `derivative_boost%`：dx/dy 增強倍率，百分比形式（預設：`170`）

**例如：**
```bash
java ImagePartialDerivatives image.png dx.png dy.png edge.png 70 200
```

## 範例輸出

### 主控台輸出
```
完成
輸入圖片: image.png
dx 輸出: output_dx.png
dy 輸出: output_dy.png
邊緣輸出: output_edge.png
強邊緣閾值: 60
dx/dy 增強倍率(%): 170
```

### 影像輸出特徵
1. **output_dx.png**：垂直邊緣為亮，水平邊緣為暗
   - x 梯度高表示從左到右亮度變化劇烈
   
2. **output_dy.png**：水平邊緣為亮，垂直邊緣為暗
   - y 梯度高表示從上到下亮度變化劇烈

3. **output_edge.png**：二值邊緣圖
   - 白色：邊緣強度超過設定閾值
   - 黑色：邊緣強度低於閾值

## 注意事項

1. 程式預設讀取同資料夾內的 `image.png`，請確認檔案存在或在指令中指定路徑。
2. 輸入圖片支援任何 ImageIO 支援的格式（PNG、JPG、BMP 等）。
3. **增強倍率調整建議**：
   - 預設 170％ 能展示清晰邊緣
   - 若要更明顯，可調至 200～220％
   - 若要抑制噪聲，可降至 120～150％
4. **邊緣閾值調整建議**：
   - 預設 60 為中度篩選
   - 若要保留更多邊緣細節，降低門檻至 30～50
   - 若要只顯示強邊緣，提高門檻至 100～150
5. 若圖片檔不存在或格式損壞，程式會拋出異常。
6. 首次執行可能較慢，因為 Java 需要進行 JIT 編譯。

## 結論

本實作以簡單而有效的方式示範影像梯度計算與邊緣檢測。透過非線性增強與可調參數，使 x、y 方向梯度的特徵更加明顯，便於觀察與分析。適合作為影像處理、電腦視覺課程的入門實驗與作業示範，也可進一步延伸為 Sobel 濾波、Canny 邊緣檢測等進階演算法的基礎。
