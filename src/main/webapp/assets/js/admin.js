/**
 * 文件: assets/js/admin.js
 * 描述: 管理员控制台的客户端交互脚本，包括模块切换、表格排序/筛选、消息提示等。
 */
document.addEventListener('DOMContentLoaded', function () {
    console.log('admin.js loaded and DOM fully parsed.');

    const sortableHeaders = document.querySelectorAll('th[data-sortable]');
    const genderFilterHeader = document.querySelector('th[data-filterable-type="gender"]'); // 学生列表的性别筛选

    sortableHeaders.forEach(header => {
        // 确保性别筛选表头不被重复处理为普通排序（如果它同时也有 data-sortable）
        if (header === genderFilterHeader && header.dataset.filterableType === 'gender') {
            return; // 性别筛选列有单独的点击处理逻辑
        }

        header.addEventListener('click', function () {
            const table = this.closest('table');
            if (!table) {
                console.warn("Sortable header clicked, but no parent table found.");
                return;
            }

            const columnIndex = parseInt(this.dataset.columnIndex);
            const columnType = this.dataset.columnType; // 获取列类型
            // 判断是否为数字类型（包括 'numeric', 'floatNumber', 'integer' 等）
            const columnTypeIsNumeric = (columnType === 'numeric' || columnType === 'floatNumber' || columnType === 'integer');
            console.log(`Header clicked: columnType='${columnType}', columnTypeIsNumeric=${columnTypeIsNumeric}`); // 调试日志

            let currentSortDir = this.dataset.sortDir || 'desc'; // 默认为降序，点击后变为升序
            const newSortDir = currentSortDir === 'asc' ? 'desc' : 'asc';

            // 重置其他表头的排序方向指示
            table.querySelectorAll('th[data-sortable]').forEach(th => {
                if (th !== this) {
                    delete th.dataset.sortDir;
                    th.classList.remove('sort-asc', 'sort-desc');
                }
            });
            this.dataset.sortDir = newSortDir; // 设置当前列的排序状态
            this.classList.remove(newSortDir === 'asc' ? 'sort-desc' : 'sort-asc'); // 移除相反的类
            this.classList.add(newSortDir === 'asc' ? 'sort-asc' : 'sort-desc');    // 添加正确的类

            console.log(`Sorting table '${table.id || 'untitled'}' by column index: ${columnIndex}, type: ${columnTypeIsNumeric ? 'numeric' : 'text'}, direction: ${newSortDir}`);

            sortTableByColumn(table, columnIndex, newSortDir === 'asc', columnTypeIsNumeric);
        });
    });

    if (genderFilterHeader) {
        let genderFilterStates = ['all', '男', '女']; // 筛选状态：全部，男，女
        let currentGenderFilterIndex = 0; // 当前筛选状态索引

        genderFilterHeader.style.cursor = 'pointer'; // 提示可点击
        updateGenderFilterDisplay(genderFilterHeader, genderFilterStates[currentGenderFilterIndex]); // 初始化显示

        genderFilterHeader.addEventListener('click', function () {
            const table = this.closest('table');
            if (!table) {
                console.warn("Gender filter header clicked, but no parent table found.");
                return;
            }
            const tbody = table.querySelector('tbody');
            if (!tbody) {
                console.warn("Gender filter header clicked, but no tbody found in table.");
                return;
            }

            const columnIndex = parseInt(this.dataset.columnIndex); // 获取性别列的索引

            // 切换到下一个筛选状态
            currentGenderFilterIndex = (currentGenderFilterIndex + 1) % genderFilterStates.length;
            const currentFilterValue = genderFilterStates[currentGenderFilterIndex];

            updateGenderFilterDisplay(this, currentFilterValue); // 更新表头显示
            console.log(`Filtering student table by gender: ${currentFilterValue}`);

            const rows = tbody.querySelectorAll('tr');
            rows.forEach(row => {
                const cell = row.querySelectorAll('td')[columnIndex];
                if (cell) {
                    const cellValue = cell.innerText.trim();
                    if (currentFilterValue === 'all' || cellValue === currentFilterValue) {
                        row.style.display = ''; // 显示符合条件的行
                    } else {
                        row.style.display = 'none'; // 隐藏不符合条件的行
                    }
                }
            });
        });
    }

    /**
     * 更新性别筛选表头的显示文本。
     * @param {HTMLElement} headerElement 性别筛选的表头元素 (th)
     * @param {string} filterState 当前的筛选状态 ('all', '男', '女')
     */
    function updateGenderFilterDisplay(headerElement, filterState) {
        let baseText = "性别"; // 表头的基础文本
        if (filterState === 'all') {
            headerElement.textContent = baseText;
        } else {
            headerElement.textContent = `${baseText} (${filterState})`; // 例如 "性别 (男)"
        }
        // 移除旧的排序/筛选指示类
        headerElement.classList.remove('sort-asc', 'sort-desc', 'filter-active');
        if (filterState !== 'all') {
            headerElement.classList.add('filter-active'); // 如果不是'all'，可以添加一个类以突出显示筛选状态
        }
    }

    /**
     * 通用的表格排序函数。
     * @param {HTMLTableElement} table 要排序的表格元素
     * @param {number} columnIndex 要排序的列索引 (0-based)
     * @param {boolean} asc 是否升序排序 (true for asc, false for desc)
     * @param {boolean} isNumeric 列是否为数字类型
     */
    function sortTableByColumn(table, columnIndex, asc = true, isNumeric = false) {
        const tbody = table.querySelector('tbody');
        if (!tbody) {
            console.warn("sortTableByColumn called, but no tbody found in table:", table.id || 'untitled');
            return;
        }

        // 获取所有当前可见的行进行排序
        const rowsToSort = Array.from(tbody.querySelectorAll('tr')).filter(row => row.style.display !== 'none');

        // 如果没有可见的行，则不执行排序
        if (rowsToSort.length === 0) {
            console.log("No visible rows to sort in table:", table.id || 'untitled');
            return;
        }

        const sortedRows = rowsToSort.sort((a, b) => {
            const cellAElement = a.querySelectorAll('td')[columnIndex];
            const cellBElement = b.querySelectorAll('td')[columnIndex];

            // 如果某一行没有足够的td（例如合并的单元格或者损坏的行结构），则将其视为相等或放在末尾
            if (!cellAElement && !cellBElement) return 0;
            if (!cellAElement) return asc ? 1 : -1; // A 无单元格，视为较大值
            if (!cellBElement) return asc ? -1 : 1; // B 无单元格，视为较大值

            const cellAValue = cellAElement.innerText.trim();
            const cellBValue = cellBElement.innerText.trim();

            let valA, valB;

            if (isNumeric) {
                valA = parseFloat(cellAValue);
                valB = parseFloat(cellBValue);
                console.log(`Numeric compare: cellA='${cellAValue}' (parsed: ${valA}), cellB='${cellBValue}' (parsed: ${valB})`); // 调试日志

                const aIsNaN = isNaN(valA);
                const bIsNaN = isNaN(valB);

                if (aIsNaN && bIsNaN) return 0;
                if (aIsNaN) return asc ? 1 : -1;  // NaN 值排在有效数字之后 (升序时)
                if (bIsNaN) return asc ? -1 : 1;  // 有效数字排在 NaN 值之前 (升序时)

                // 两个都是有效数字
                if (valA < valB) return asc ? -1 : 1;
                if (valA > valB) return asc ? 1 : -1;
                return 0;

            } else { // 文本排序
                valA = cellAValue.toLowerCase();
                valB = cellBValue.toLowerCase();
                // console.log(`Text compare: '${valA}' vs '${valB}'`); // 调试日志

                if (valA === '' && valB !== '') return asc ? 1 : -1;
                if (valA !== '' && valB === '') return asc ? -1 : 1;
                if (valA === '' && valB === '') return 0;

                // 使用 localeCompare 进行更准确的字符串比较 (推荐)
                return asc ? valA.localeCompare(valB) : valB.localeCompare(valA);
                // 或者简单的比较：
                // if (valA < valB) return asc ? -1 : 1;
                // if (valA > valB) return asc ? 1 : -1;
                // return 0;
            }
        });

        // 将排序后的行（仅可见行）重新添加到tbody的末尾
        // 这会保持未参与排序的隐藏行的位置（如果它们还在tbody中）
        // 如果希望所有行（包括隐藏行）都参与排序并且tbody被完全重构，则需要不同的处理
        tbody.append(...sortedRows);
    }

    const sidebarLinks = document.querySelectorAll('.sidebar ul li a');
    const contentModules = document.querySelectorAll('.admin-main .module');

    function showModule(moduleIdToShow) {
        if (!moduleIdToShow) {
            console.warn("showModule called with invalid moduleId:", moduleIdToShow);
            return;
        }

        let moduleFound = false;
        contentModules.forEach(module => {
            if (module.id === moduleIdToShow) {
                module.classList.add('active');
                moduleFound = true;
            } else {
                module.classList.remove('active');
            }
        });

        if (!moduleFound) {
            console.warn("Module with ID '" + moduleIdToShow + "' not found. Cannot activate.");
            // 可以选择显示一个默认模块或记录错误
            return;
        }

        document.querySelectorAll('.sidebar ul li').forEach(listItem => {
            if (listItem.dataset.moduleId === moduleIdToShow) {
                listItem.classList.add('active');
            } else {
                listItem.classList.remove('active');
            }
        });

        localStorage.setItem('activeAdminModule', moduleIdToShow);
        console.log("Active module set to:", moduleIdToShow);
    }

    sidebarLinks.forEach(link => {
        link.addEventListener('click', function (event) {
            // 从 href 获取模块 ID, 例如 "#student" -> "student"
            const moduleId = this.getAttribute('href').substring(1);
            if (document.getElementById(moduleId)) {
                // event.preventDefault(); // 如果不希望改变URL hash，可以取消注释
                showModule(moduleId);
            } else {
                console.warn("Clicked link for non-existent module:", moduleId);
            }
        });
    });

    // 页面加载时确定并激活模块
    let initialActiveModuleId = 'student'; // 默认模块
    const urlParams = new URLSearchParams(window.location.search);
    const moduleFromUrlParam = urlParams.get('activeModule');

    if (moduleFromUrlParam && document.getElementById(moduleFromUrlParam)) {
        initialActiveModuleId = moduleFromUrlParam;
        localStorage.setItem('activeAdminModule', initialActiveModuleId);
        // （可选）从URL中移除参数，保持URL整洁
        if (window.history.replaceState) {
            const currentUrl = new URL(window.location.href);
            currentUrl.searchParams.delete('activeModule');
            window.history.replaceState({ path: currentUrl.href }, '', currentUrl.href);
        }
    } else {
        const storedModuleId = localStorage.getItem('activeAdminModule');
        if (storedModuleId && document.getElementById(storedModuleId)) {
            initialActiveModuleId = storedModuleId;
        }
        else if (window.location.hash) { // #student
            const hashModuleId = window.location.hash.substring(1);
            if (document.getElementById(hashModuleId)) {
                initialActiveModuleId = hashModuleId;
                localStorage.setItem('activeAdminModule', initialActiveModuleId);
            }
        }
    }
    showModule(initialActiveModuleId); // 应用初始模块

    // Flash 消息自动隐藏
    const successMessageFlash = document.querySelector('.alert-success');
    if (successMessageFlash) {
        setTimeout(() => {
            successMessageFlash.style.opacity = '0'; // 开始淡出
            // 在淡出动画完成后实际移除或隐藏元素
            setTimeout(() => successMessageFlash.style.display = 'none', 500); // 假设淡出动画0.5秒
        }, 3000); // 3秒后开始隐藏
    }

    const errorMessageFlash = document.querySelector('.alert-danger');
    if (errorMessageFlash) {
        setTimeout(() => {
            errorMessageFlash.style.opacity = '0';
            setTimeout(() => errorMessageFlash.style.display = 'none', 500);
        }, 5000); // 错误消息显示时间稍长
    }

    console.log("Admin dashboard script fully loaded and initialized with all features.");
});
