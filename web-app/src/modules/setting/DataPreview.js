import React from "react";
import Grid from "@material-ui/core/Grid/Grid";
import Input from "@material-ui/core/Input/Input";
import InputAdornment from "@material-ui/core/InputAdornment/InputAdornment";
import Search from "@material-ui/core/SvgIcon/SvgIcon";
import FormControl from "@material-ui/core/FormControl/FormControl";
import Checkbox from "@material-ui/core/Checkbox/Checkbox";
import RadioGroup from "@material-ui/core/RadioGroup/RadioGroup";
import FormControlLabel from "@material-ui/core/FormControlLabel/FormControlLabel";
import {VERIFICATION_TYPE, GET_LCR_DATA_URL} from "../../const";
import Radio from "@material-ui/core/Radio/Radio";
import Button from "@material-ui/core/Button/Button";
import LcrTable from "./LcrTable";
import Tooltip from "@material-ui/core/Tooltip/Tooltip";
import update from 'immutability-helper';

export default class extends React.Component {

    state = {
        items: [],
        itemsIds: {},
        pageNumber: 0,
        pageSize: 100,
        totalItemsCount: 0,
        searchMode: false,
        searchInput: "",
        hideHiddenCheckbox: false,
    };

    componentDidUpdate(prevProps) {
        if (prevProps.filterValues !== this.props.filterValues) {
            this.setState({
                items: [],
                itemsIds: {}
            }, async () => {
                await this.fetchData()
            });
        }
    }

    fetchData = async () => {

        if (this.props.filterValues.products.length === 0 &&
            this.props.filterValues.prefixZones.length === 0 &&
            this.props.filterValues.countries.length === 0 &&
            this.props.filterValues.operators.length === 0 &&
            this.props.filterValues.prefix === '') {

            this.props.setStateDescription('Не задан ни один фильтр');
            return;
        }

        this.props.startShowProgress();
        this.props.setStateDescription('Выполняется загрузка данных для сверки');
        this.props.onExcludedItemsChange([]);

        const pagingFilter = Object.assign({}, this.props.filterValues);
        pagingFilter.pageNumber = this.state.pageNumber;
        pagingFilter.pageSize = this.state.pageSize;
        pagingFilter.fromDate = this.props.reportPeriod.fromDate.toISOString().slice(0, 10);
        pagingFilter.toDate = this.props.reportPeriod.toDate.toISOString().slice(0, 10);

        try {
            const response = await fetch(GET_LCR_DATA_URL, {
                method: "POST",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(pagingFilter)
            });

            const json = await response.json();

            if (response.ok) {
                // при пагинации и изменении кол-ва элементов на странице - можно получить ситуацию,
                // когда будет запрос на данные, которые уже загружены. поэтому запоминаем ид элементов и повторяющиеся игнорируем
                const itemsForAdd = [];
                const idsForAdd = [];
                json.content.forEach(i => {
                    if (!this.state.itemsIds[i.id]) {
                        i.isExcluded = false;
                        idsForAdd.push(i.id);
                        itemsForAdd.push(i);
                    }
                });

                const newItems = this.state.items.concat(itemsForAdd);
                const newItemsIds = Object.assign({}, this.state.itemsIds);
                idsForAdd.forEach(i => newItemsIds[i] = 1);

                this.setState({
                    items: newItems,
                    itemsIds: newItemsIds,
                    pageNumber: json.number,
                    totalItemsCount: json.totalElements
                });

                this.props.stopShowProgress();
                this.props.setStateDescription('Загрузка данных для сверки успешно завершена');
            } else {
                throw new Error(json.error + ' (' + json.status + '): ' + json.message);
            }
        } catch (e) {
            this.props.stopShowProgress();
            this.props.setStateDescription('Загрузка данных для сверки завершена с ошибкой: ' + e);
        }
    };

    onPageChange = (event, newPage) => {
        if (this.props.showProgress) {
            return;
        }
        if (this.state.searchMode) {
            this.setState({
                pageNumber: newPage
            });
        } else {
            if ((newPage + 1) * this.state.pageSize > this.state.items.length
                && this.state.items.length < this.state.totalItemsCount) {
                this.setState({
                    pageNumber: newPage
                }, async () => {
                    await this.fetchData()
                });
            } else {
                this.setState({
                    pageNumber: newPage
                });
            }
        }
    };

    onPageSizeChange = (event) => {
        if (this.props.showProgress) {
            return;
        }
        const newPageSize = parseInt(event.target.value);
        const newPage = 0;

        if (this.state.searchMode) {
            this.setState({
                pageNumber: newPage,
                pageSize: newPageSize
            });
        } else {
            if ((newPage + 1) * newPageSize > this.state.items.length
                && this.state.items.length < this.state.totalItemsCount) {
                this.setState({
                    pageNumber: newPage,
                    pageSize: newPageSize
                }, async () => {
                    await this.fetchData()
                });
            } else {
                this.setState({
                    pageNumber: newPage,
                    pageSize: newPageSize
                });
            }
        }
    };

    onSearchChange = (event) => {
        let searchInput = event.target.value;
        this.setState({
            searchInput: searchInput,
            searchMode: searchInput !== ''
        });
    };

    onHiddenVisibleChange = (event) => {
        this.setState(prevState => ({
            hideHiddenCheckbox: !prevState.hideHiddenCheckbox
        }))
    };

    onExcludeItem = (item) => {
        const itemIndex = this.state.items.indexOf(item);
        this.setState({
            items: update(this.state.items,
                {
                    [itemIndex]: {
                        isExcluded: {
                            $apply: function (x) {
                                return !x;
                            }
                        }
                    }
                })
        }, () => {
            this.props.onExcludedItemsChange(this.state.items.filter(i => i.isExcluded).map(i => i.id))
        });
    };

    onReportTypeChange = (event) => {
        this.props.onReportTypeChange(event.target.value);
    };

    filterBySearch() {
        const regexp = new RegExp(this.state.searchInput, "i");
        return this.state.items.filter(i =>
            regexp.test(i.price) ||
            regexp.test(i.operator) ||
            regexp.test(i.country) ||
            regexp.test(i.product) ||
            regexp.test(i.prefixZone) ||
            regexp.test(i.prefix) ||
            regexp.test(i.fromDate) ||
            regexp.test(i.toDate));
    }

    render() {

        const buttonsDisabled = this.props.showProgress;

        const pageNumber = this.state.pageNumber;
        const pageSize = this.state.pageSize;
        let filteredItems;
        let totalItemsCount;

        if (this.state.searchMode) {
            filteredItems = this.filterBySearch();
            totalItemsCount = filteredItems.length;
        } else {
            filteredItems = this.state.items;
            totalItemsCount = this.state.totalItemsCount;
        }

        filteredItems = filteredItems.slice(pageNumber * pageSize, (pageNumber + 1) * pageSize);

        if (this.state.hideHiddenCheckbox) {
            filteredItems = filteredItems.filter(item => !item.isHidden);
        }

        return (
            <>
                <Grid container spacing={1}>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Tooltip title="Только по загруженным (просмотренным)">
                            <Input value={this.state.searchInput}
                                   disabled={buttonsDisabled}
                                   onChange={this.onSearchChange}
                                   placeholder={"Поиск (по загруженным) "}
                                   endAdornment={
                                       <InputAdornment position="end">
                                           <Search/>
                                       </InputAdornment>}/>
                        </Tooltip>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <FormControl>
                            <Tooltip title="Не вызывает перестроение страниц, просто скрывает">
                                <label>
                                    <Checkbox checked={this.state.hideHiddenCheckbox}
                                              disabled={buttonsDisabled}
                                              onChange={this.onHiddenVisibleChange}/>
                                    Скрыть свернутые
                                </label>
                            </Tooltip>
                        </FormControl>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <FormControl>
                            <RadioGroup value={this.props.reportType} onChange={this.onReportTypeChange}>
                                <FormControlLabel value={VERIFICATION_TYPE.byPrefixes}
                                                  disabled={buttonsDisabled}
                                                  control={<Radio/>}
                                                  label="По префиксам"/>
                                <FormControlLabel value={VERIFICATION_TYPE.byCount}
                                                  disabled={buttonsDisabled}
                                                  control={<Radio/>}
                                                  label="По количеству"/>
                            </RadioGroup>
                        </FormControl>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Tooltip title="По выбранным фильтрам и исключениям">
                            <Button onClick={this.props.handleBeginVerification}
                                    disabled={buttonsDisabled}
                                    variant="contained" color="primary" size="small">
                                Создать отчет
                            </Button>
                        </Tooltip>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Tooltip title="Создать отчет и сохранить в файл (по выбранным фильтрам и исключениям)">
                            <Button onClick={this.props.handleSaveReport}
                                    disabled={buttonsDisabled}
                                    variant="contained" size="small">
                                Сохранить отчет
                            </Button>
                        </Tooltip>
                    </Grid>

                </Grid>

                <LcrTable items={filteredItems}
                          pageNumber={pageNumber}
                          pageSize={pageSize}
                          totalItemsCount={totalItemsCount}
                          buttonsDisabled={buttonsDisabled}
                          onExcludeItem={this.onExcludeItem}
                          onPageChange={this.onPageChange}
                          onPageSizeChange={this.onPageSizeChange}/>
            </>
        );
    }
}