// admin.js - 管理员界面的 JavaScript 脚本

document.addEventListener('DOMContentLoaded', function() {
    console.log('admin.js loaded and DOM fully parsed.');

    const sortableHeaders = document.querySelectorAll('th[data-sortable]');
    const genderFilterHeader = document.querySelector('th[data-filterable-type="gender"]');

    sortableHeaders.forEach(header => {
        // 确保性别筛选表头不被重复处理为普通排序（如果它同时也有 data-sortable）
        if (header === genderFilterHeader && header.dataset.filterableType === 'gender') {
            return;
        }

        header.addEventListener('click', function() {
            const table = this.closest('table');
            if (!table) return;

            const columnIndex = parseInt(this.dataset.columnIndex);
            const columnTypeIsNumeric = this.dataset.columnType === 'numeric';
            
            let currentSortDir = this.dataset.sortDir || 'desc';
            const newSortDir = currentSortDir === 'asc' ? 'desc' : 'asc';
            
            // 重置其他表头的排序方向指示
            table.querySelectorAll('th[data-sortable]').forEach(th => {
                if (th !== this) {
                    delete th.dataset.sortDir; // 移除其他列的排序状态
                    th.classList.remove('sort-asc', 'sort-desc'); // 移除其他列的指示器
                }
            });
            this.dataset.sortDir = newSortDir; // 设置当前列的排序状态

            console.log(`Sorting table by column index: ${columnIndex}, type: ${columnTypeIsNumeric ? 'numeric' : 'text'}, direction: ${newSortDir}`);
            
            sortTableByColumn(table, columnIndex, newSortDir === 'asc', columnTypeIsNumeric);
        });
    });

    if (genderFilterHeader) {
        let genderFilterStates = ['all', '男', '女']; // 筛选状态：全部，男，女
        let currentGenderFilterIndex = 0; // 当前筛选状态索引

        genderFilterHeader.style.cursor = 'pointer'; // 提示可点击
        updateGenderFilterDisplay(genderFilterHeader, genderFilterStates[currentGenderFilterIndex]);


        genderFilterHeader.addEventListener('click', function() {
            const table = this.closest('table');
            if (!table) return;
            const tbody = table.querySelector('tbody');
            if (!tbody) return;

            const columnIndex = parseInt(this.dataset.columnIndex);

            // 切换到下一个筛选状态
            currentGenderFilterIndex = (currentGenderFilterIndex + 1) % genderFilterStates.length;
            const currentFilterValue = genderFilterStates[currentGenderFilterIndex];
            
            updateGenderFilterDisplay(this, currentFilterValue);
            console.log(`Filtering by gender: ${currentFilterValue}`);

            const rows = tbody.querySelectorAll('tr');
            rows.forEach(row => {
                const cell = row.querySelectorAll('td')[columnIndex];
                if (cell) {
                    const cellValue = cell.innerText.trim();
                    if (currentFilterValue === 'all' || cellValue === currentFilterValue) {
                        row.style.display = ''; // 显示行
                    } else {
                        row.style.display = 'none'; // 隐藏行
                    }
                }
            });
        });
    }
});

function updateGenderFilterDisplay(headerElement, filterState) {
    let baseText = "性别";
    if (filterState === 'all') {
        headerElement.textContent = baseText;
    } else {
        headerElement.textContent = `${baseText} (${filterState})`;
    }
    // 移除旧的排序/筛选类，添加新的筛选类（如果需要特定样式）
    headerElement.classList.remove('sort-asc', 'sort-desc', 'filter-active');
    if (filterState !== 'all') {
        headerElement.classList.add('filter-active');
    }
}


/**
 * 通用的表格排序函数
 * @param {HTMLTableElement} table 要排序的表格元素
 * @param {number} columnIndex 要排序的列索引 (0-based)
 * @param {boolean} asc 是否升序排序
 * @param {boolean} isNumeric 列是否为数字类型
 */
function sortTableByColumn(table, columnIndex, asc = true, isNumeric = false) {
    const tbody = table.querySelector('tbody');
    const rows = Array.from(tbody.querySelectorAll('tr'));

    const sortedRows = rows.sort((a, b) => {
        const cellA = a.querySelectorAll('td')[columnIndex].innerText.trim();
        const cellB = b.querySelectorAll('td')[columnIndex].innerText.trim();

        let valA = isNumeric ? parseFloat(cellA) : cellA;
        let valB = isNumeric ? parseFloat(cellB) : cellB;

        // 处理空值或非数字的情况，使其排在最后
        if (isNumeric) {
            if (isNaN(valA) && !isNaN(valB)) return asc ? 1 : -1;
            if (!isNaN(valA) && isNaN(valB)) return asc ? -1 : 1;
            if (isNaN(valA) && isNaN(valB)) return 0;
        } else {
            if (valA === '' && valB !== '') return asc ? 1 : -1;
            if (valA !== '' && valB === '') return asc ? -1 : 1;
            if (valA === '' && valB === '') return 0;
        }


        if (valA < valB) {
            return asc ? -1 : 1;
        }
        if (valA > valB) {
            return asc ? 1 : -1;
        }
        return 0;
    });

    // 移除现有行
    while (tbody.firstChild) {
        tbody.removeChild(tbody.firstChild);
    }

    // 添加排序后的行
    tbody.append(...sortedRows);

    // 更新表头排序指示器 (可选)
    table.querySelectorAll('th').forEach(th => th.classList.remove('sort-asc', 'sort-desc'));
    const headerToSort = table.querySelectorAll('th')[columnIndex];
    if (headerToSort) {
        headerToSort.classList.add(asc ? 'sort-asc' : 'sort-desc');
    }
}

// 您可以在 admin.css 中添加排序指示器的样式：
// th.sort-asc::after { content: ' ▲'; }
// th.sort-desc::after { content: ' ▼'; }
// th[data-sortable]:hover { cursor: pointer; background-color: #e8f0fe; }