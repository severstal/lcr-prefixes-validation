import React from "react";
import {VERIFICATION_TYPE, VERIFY_BY_COUNT_URL, VERIFY_BY_PREFIXES_URL} from "../../const";
import ResultTablePrefixes from "./ResultTablePrefixes";
import ResultTableCounts from "./ResultTableCounts";
import Grid from "@material-ui/core/Grid/Grid";
import Button from "@material-ui/core/Button/Button";
import Input from "@material-ui/core/Input/Input";
import InputAdornment from "@material-ui/core/InputAdornment/InputAdornment";
import Search from '@material-ui/icons/Search';
import {REPORT_TYPE} from "./ReportTableOptions";

export default class extends React.Component {

    state = {
        reportItems: [],
        reportType: VERIFICATION_TYPE.byPrefixes,
        search: ''
    };

    async componentDidUpdate(prevProps) {
        if (prevProps.filterValues !== this.props.filterValues ||
            prevProps.reportType !== this.props.reportType ||
            prevProps.excludedItems !== this.props.excludedItems) {
            await this.handleBeginVerification();
        }
    };

    handleBeginVerification = async () => {

        if (this.props.filterValues.products.length === 0 &&
            this.props.filterValues.prefixZones.length === 0 &&
            this.props.filterValues.countries.length === 0 &&
            this.props.filterValues.operators.length === 0 &&
            this.props.filterValues.prefix === '') {
            this.props.setStateDescription('Не задан ни один фильтр');
            return;
        }

        this.props.startShowProgress();
        this.props.setStateDescription('Выполняется формирование отчета сверки');
        this.setState({reportItems: [], reportType: this.props.reportType});

        const filter = Object.assign({}, this.props.filterValues);
        filter.excludedItems = this.props.excludedItems;
        filter.fromDate = this.props.reportPeriod.fromDate.toISOString().slice(0, 10);
        filter.toDate = this.props.reportPeriod.toDate.toISOString().slice(0, 10);

        const url = this.props.reportType === VERIFICATION_TYPE.byPrefixes
            ? VERIFY_BY_PREFIXES_URL
            : VERIFY_BY_COUNT_URL;

        try {
            const response = await fetch(url, {
                method: "POST",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(filter)
            });

            const json = await response.json();

            if (response.ok) {
                this.setState({reportItems: json});
                this.props.stopShowProgress();
                this.props.setStateDescription('Отчет сверки успешно сформирован');
            } else {
                throw new Error(json.error + ' (' + json.status + '): ' + json.message);
            }
        } catch (e) {
            this.props.stopShowProgress();
            this.props.setStateDescription('Формирование отчета сверки завершено с ошибкой: ' + e);
        }
    };

    handleSearchChange = (event) => {
        this.setState({search: event.target.value});
    };

    handleUnloadToCsv = (event) => {
        const fileName = 'verify-' + Date.now();
        const headers = REPORT_TYPE[this.state.reportType].headers;
        this.exportCSVFile(headers, this.state.reportItems.slice(), fileName);
    };

    exportCSVFile = (headers, items, fileTitle) => {
        if (headers) {
            items.unshift(headers);
        }

        const jsonObject = JSON.stringify(items);
        const csv = this.convertToCSV(jsonObject);
        const exportedFilename = fileTitle + '.csv' || 'export.csv';

        const blob = new Blob([csv], {type: 'text/csv;charset=utf-8;'});
        if (navigator.msSaveBlob) { // IE 10+
            navigator.msSaveBlob(blob, exportedFilename);
        } else {
            const link = document.createElement("a");
            if (link.download !== undefined) { // feature detection
                // Browsers that support HTML5 download attribute
                const url = URL.createObjectURL(blob);
                link.setAttribute("href", url);
                link.setAttribute("download", exportedFilename);
                link.style.visibility = 'hidden';
                document.body.appendChild(link);
                link.click();
                document.body.removeChild(link);
            }
        }
    };

    convertToCSV = (objArray) => {
        const array = typeof objArray !== 'object' ? JSON.parse(objArray) : objArray;
        let str = '';

        for (let i = 0; i < array.length; i++) {
            let line = '';
            for (let index in array[i]) {
                if (line !== '') {
                    line += ',';
                }
                line += array[i][index];
            }
            str += line + '\r\n';
        }

        return str;
    };

    render() {

        const buttonsDisabled = this.props.showProgress;

        const isPrefixes = this.state.reportType === VERIFICATION_TYPE.byPrefixes;

        const reportItems = REPORT_TYPE[this.state.reportType].search(this.state.reportItems, this.state.search);

        return (
            <>
                <br/>

                <Grid container spacing={3}>
                    <Grid item>
                        <Button variant="contained" size="small"
                                disabled={true}>{REPORT_TYPE[this.state.reportType].title}</Button>
                    </Grid>
                    <Grid item>
                        <Input value={this.state.search}
                               disabled={buttonsDisabled}
                               onChange={this.handleSearchChange}
                               placeholder={"Поиск (для поиска строки) "}
                               endAdornment={<InputAdornment position="end">
                                   <Search/>
                               </InputAdornment>}
                        />
                    </Grid>
                    <Grid item>
                        <Button variant="contained"
                                size="small"
                                color="primary"
                                disabled={buttonsDisabled}
                                onClick={this.handleUnloadToCsv}>
                            Выгрузить в csv
                        </Button>
                    </Grid>
                </Grid>

                <br/>

                {isPrefixes
                    ? <ResultTablePrefixes reportItems={reportItems} disabled={buttonsDisabled}/>
                    : <ResultTableCounts reportItems={reportItems} disabled={buttonsDisabled}/>}

            </>
        );
    }
}
