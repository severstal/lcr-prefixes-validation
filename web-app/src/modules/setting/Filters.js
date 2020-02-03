import React from "react";
import Grid from "@material-ui/core/Grid/Grid";
import DropdownSelect from "./DropdownSelect";
import Input from "@material-ui/core/Input/Input";
import Button from "@material-ui/core/Button/Button";
import Clear from '@material-ui/icons/Clear';
import PlayArrow from '@material-ui/icons/PlayArrow';
import Tooltip from "@material-ui/core/Tooltip/Tooltip";
import {GET_LCR_FILTERS_URL, LCR_PREPARE_URL} from "../../const";

export default class extends React.Component {

    state = {
        filterProductValues: [], // todo values: {...} ?
        filterPrefixZoneValues: [],
        filterCountryValues: [],
        filterOperatorValues: [],
        filterPrefix: '',
        options: {
            products: [],
            prefixZones: [],
            countries: [],
            operators: [],
            country2PrefixZones: []
        },
    };

    async componentDidUpdate(prevProps) {
        if (prevProps.reportPeriod.toDate !== this.props.reportPeriod.toDate ||
            prevProps.reportPeriod.fromDate !== this.props.reportPeriod.fromDate) {
            await this.handleLoadLcrFilters();
        }
    };

    handleLoadLcrFilters = async () => {
        this.setState({
            options: {
                products: [],
                prefixZones: [],
                countries: [],
                operators: [],
                country2PrefixZones: []
            }
        });
        this.onClearFilters();

        this.props.startShowProgress();
        this.props.setStateDescription('Выполняется загрузка фильтров');

        const to = this.props.reportPeriod.toDate;
        const from = this.props.reportPeriod.fromDate;

        try {
            await fetch(LCR_PREPARE_URL + `?fromDate=${encodeURIComponent(from)}&toDate=${encodeURIComponent(to)}`);

            const response = await fetch(GET_LCR_FILTERS_URL);
            const json = await response.json();

            if (response.ok) {
                this.props.stopShowProgress();
                this.props.setStateDescription('Загрузка фильтров успешно завершена');
                this.setState({options: json});
            } else {
                throw new Error(json.error + ' (' + json.status + '): ' + json.message);
            }
        } catch (e) {
            this.props.stopShowProgress();
            this.props.setStateDescription('Загрузка фильтров завершена с ошибкой: ' + e);
        }
    };

    onClearFilters = () => {
        this.setState({
            filterProductValues: [],
            filterPrefixZoneValues: [],
            filterCountryValues: [],
            filterOperatorValues: [],
            filterPrefix: ''
        }, () => {
            this.props.onFilterValuesChange(this.composeLcrFiltersDto())
        });
    };

    onProductFilterChange = (event) => {
        this.setState({filterProductValues: event}, () => {
            this.props.onFilterValuesChange(this.composeLcrFiltersDto())
        });
    };

    onOperatorFilterChange = (event) => {
        this.setState({filterOperatorValues: event}, () => {
            this.props.onFilterValuesChange(this.composeLcrFiltersDto())
        });
    };

    onCountryFilterChange = (event) => {
        this.setState({filterCountryValues: event}, () => {
            this.props.onFilterValuesChange(this.composeLcrFiltersDto())
        });
    };

    onPrefixZoneFilterChange = (event) => {
        this.setState({filterPrefixZoneValues: event}, () => {
            this.props.onFilterValuesChange(this.composeLcrFiltersDto())
        });
    };

    onPrefixFilterChange = (event) => {
        this.setState({filterPrefix: event.target.value}, () => {
            this.props.onFilterValuesChange(this.composeLcrFiltersDto())
        });
    };

    composeLcrFiltersDto = () => {
        return {
            products: this.state.filterProductValues.map(i => i.value),
            prefixZones: this.state.filterPrefixZoneValues.map(i => i.value),
            countries: this.state.filterCountryValues.map(i => i.value),
            operators: this.state.filterOperatorValues.map(i => i.value),
            prefix: this.state.filterPrefix,
        };
    };

    render() {

        const buttonsDisabled = this.props.showProgress;

        const filterProductOptions = this.state.options.products.map(item => {
            return {'value': item}
        });
        const filterOperatorOptions = this.state.options.operators.map(item => {
            return {'value': item}
        });
        const filterCountryOptions = this.state.options.countries.map(item => {
            return {'value': item}
        });

        let filterPrefixZoneOptions = [];

        if (this.state.filterCountryValues.length > 0) {
            let filterCountryValues = this.state.filterCountryValues.map(item => {
                return item.value;
            });
            filterCountryValues.forEach(item => {
                filterPrefixZoneOptions = filterPrefixZoneOptions.concat(this.state.options.country2PrefixZones[item]);

            });
        } else {
            filterPrefixZoneOptions = this.state.options.prefixZones;
        }

        filterPrefixZoneOptions = filterPrefixZoneOptions.filter(item => item).map(item => {
            return {'value': item}
        });

        return (
            <>
                <Grid container spacing={2}>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <span>Фильтр</span>
                    </Grid>

                    <Grid item>
                        <DropdownSelect options={filterProductOptions}
                                        disabled={buttonsDisabled}
                                        values={this.state.filterProductValues}
                                        label={"Продукт"}
                                        onChange={this.onProductFilterChange}/>
                    </Grid>

                    <Grid item>
                        <DropdownSelect options={filterOperatorOptions}
                                        disabled={buttonsDisabled}
                                        values={this.state.filterOperatorValues}
                                        label={"Оператор"}
                                        onChange={this.onOperatorFilterChange}/>
                    </Grid>

                    <Grid item>
                        <DropdownSelect options={filterCountryOptions}
                                        disabled={buttonsDisabled}
                                        values={this.state.filterCountryValues}
                                        label={"Страна"}
                                        onChange={this.onCountryFilterChange}/>
                    </Grid>

                    <Grid item>
                        <DropdownSelect options={filterPrefixZoneOptions}
                                        disabled={buttonsDisabled}
                                        values={this.state.filterPrefixZoneValues}
                                        label={"Зона"}
                                        onChange={this.onPrefixZoneFilterChange}/>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Input value={this.state.filterPrefix}
                               disabled={buttonsDisabled}
                               onChange={this.onPrefixFilterChange}
                               placeholder="Префикс"
                               style={{maxWidth: '70px'}}/>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Tooltip title="Сбросить фильтр">
                            <Button onClick={this.onClearFilters}
                                    disabled={buttonsDisabled}
                                    variant="contained"
                                    size="small">
                                <Clear/>
                            </Button>
                        </Tooltip>
                    </Grid>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Tooltip title="Применить фильтр/загрузить префиксы LCR">
                            <Button onClick={this.props.handleApplyFilters}
                                    disabled={buttonsDisabled}
                                    variant="contained"
                                    color="primary"
                                    size="small">
                                <PlayArrow/>
                            </Button>
                        </Tooltip>
                    </Grid>

                </Grid>
            </>
        );
    }
}