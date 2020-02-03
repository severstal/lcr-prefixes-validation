import React from 'react';
import ReactDOM from 'react-dom';
import Container from '@material-ui/core/Container';
import TabPanel from './TabPanel';
import {GET_REPORT_BY_COUNT_URL, GET_REPORT_BY_PREFIXES_URL, MITM_URL, VERIFICATION_TYPE} from "./const";
import Report from "./modules/report/Report";
import Tabs from "@material-ui/core/Tabs/Tabs";
import Tab from "@material-ui/core/Tab/Tab";
import CircularProgress from "@material-ui/core/CircularProgress/CircularProgress";
import PrepareAndLoadFilters from "./modules/setting/PrepareAndLoadFilters";
import Filters from "./modules/setting/Filters";
import DataPreview from "./modules/setting/DataPreview";
import streamSaver from "streamsaver";

class App extends React.Component {

    state = {
        tab: 0,
        stateDescription: 'Выберите даты и начните выгрузку',
        showProgress: false,
        reportPeriod: {
            fromDate: new Date(new Date().setMonth(new Date().getMonth() - 1)),
            toDate: new Date(),
        },
        filter: {
            reportPeriod: {
                fromDate: null,
                toDate: null
            },
            values: {
                products: [],
                prefixZones: [],
                countries: [],
                operators: [],
                prefix: ''
            },
        },
        dataPreview: {
            reportType: VERIFICATION_TYPE.byPrefixes,
            filterValues: {
                products: [],
                prefixZones: [],
                countries: [],
                operators: [],
                prefix: ''
            },
            excludedItems: []
        },
        report: {
            reportType: null,
            filterValues: {
                products: [],
                prefixZones: [],
                countries: [],
                operators: [],
                prefix: ''
            }
        }
    };

    onTabChange = (event, tab) => this.setState({tab});

    onReportPeriodChange = (reportPeriod) => this.setState({reportPeriod});

    startShowProgress = () => this.setState({showProgress: true});
    stopShowProgress = () => this.setState({showProgress: false});

    setStateDescription = (stateDescription) => this.setState({stateDescription});

    onFilterValuesChange = (filterValues) => {
        this.setState(prevState => ({
            filter: {
                ...prevState.filter,
                values: filterValues
            }
        }));
    };

    onReportTypeChange = (reportType) => {
        this.setState(prevState => ({
            dataPreview: {
                ...prevState.dataPreview,
                reportType: reportType
            }
        }));
    };

    onExcludedItemsChange = (excludedItems) => {
        this.setState(prevState => ({
            dataPreview: {
                ...prevState.dataPreview,
                excludedItems: excludedItems
            }
        }));
    };

    handleLoadLcrFilters = () => {
        this.setState(prevState => ({
            filter: {
                ...prevState.filter,
                reportPeriod: {
                    toDate: prevState.reportPeriod.toDate.toISOString().slice(0, 10),
                    fromDate: prevState.reportPeriod.fromDate.toISOString().slice(0, 10)
                }
            }
        }));
    };

    handleApplyFilters = () => {
        this.setState(prevState => ({
                dataPreview: {
                    ...prevState.dataPreview,
                    filterValues: prevState.filter.values
                }
            })
        );
    };

    handleBeginVerification = () => {
        this.setState(prevState => ({
            tab: 1,
            report: {
                filterValues: prevState.filter.values,
                reportType: prevState.dataPreview.reportType,
                excludedItems: prevState.dataPreview.excludedItems
            }
        }));
    };

    handleSaveReport = async () => {

        if (this.state.filter.values.products.length === 0 &&
            this.state.filter.values.prefixZones.length === 0 &&
            this.state.filter.values.countries.length === 0 &&
            this.state.filter.values.operators.length === 0 &&
            this.state.filter.values.prefix === '') {
            this.setStateDescription('Не задан ни один фильтр');
            return;
        }

        this.startShowProgress();
        this.setStateDescription('Выполняется формирование файла отчета');

        const filter = Object.assign({}, this.state.filter.values);
        filter.fromDate = this.state.reportPeriod.fromDate.toISOString().slice(0, 10);
        filter.toDate = this.state.reportPeriod.toDate.toISOString().slice(0, 10);
        filter.excludedItems = this.state.dataPreview.excludedItems;

        const url = this.state.dataPreview.reportType === VERIFICATION_TYPE.byPrefixes ?
            GET_REPORT_BY_PREFIXES_URL :
            GET_REPORT_BY_COUNT_URL;

        try {
            const response = await fetch(url, {
                method: "POST",
                headers: {
                    'Accept': 'application/json',
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(filter)
            });

            if (response.ok) {
                const fileName = 'report-' + this.state.dataPreview.reportType + '-' + Date.now() + '.csv';

/* браузер падает при размере файла около 1Гб
                const json = await response.json();
                const blob = new Blob(json.body, {type: 'text/csv;charset=utf-8;'});
                if (navigator.msSaveBlob) { // IE 10+
                    navigator.msSaveBlob(blob, fileName);
                } else {
                    const link = document.createElement("a");
                    if (link.download !== undefined) { // feature detection
                        // Browsers that support HTML5 download attribute
                        const url = URL.createObjectURL(blob);
                        link.setAttribute("href", url);
                        link.setAttribute("download", fileName);
                        link.style.visibility = 'hidden';
                        document.body.appendChild(link);
                        link.click();
                        document.body.removeChild(link);
                    }
                }
*/

                streamSaver.mitm = MITM_URL;
                const fileStream = streamSaver.createWriteStream(fileName);
                response.body.pipeTo(fileStream);

                this.stopShowProgress();
                this.setStateDescription('Файл отчета успешно сформирован');
            } else {
                const json = await response.json();
                throw new Error(json.error + ' (' + json.status + '): ' + json.message);
            }
        } catch (e) {
            this.stopShowProgress();
            this.setStateDescription('Загрузка данных для сверки завершена с ошибкой: ' + e);
        }
    };

    render() {
        return (
            <>
                {this.state.showProgress && <CircularProgress size={150}
                                                              style={{
                                                                  position: 'absolute',
                                                                  top: '20%',
                                                                  left: '45%',
                                                                  zIndex: 1
                                                              }}/>}

                <Container style={{background: '#EEEEEE'}}>
                    <div
                        style={{width: '100%', display: 'flex', alignItems: 'center', justifyContent: 'space-between'}}>
                        <div>
                            <h2>Модуль сверки префиксов</h2>
                        </div>
                        <div>
                            <span>{this.state.stateDescription}</span>
                        </div>
                    </div>

                    <Tabs value={this.state.tab} onChange={this.onTabChange}>
                        <Tab label="Сверка"/>
                        <Tab label="Отчет"/>
                    </Tabs>

                    <TabPanel value={this.state.tab} index={0}>

                        <PrepareAndLoadFilters reportPeriod={this.state.reportPeriod}
                                               onReportPeriodChange={this.onReportPeriodChange}
                                               handleLoadLcrFilters={this.handleLoadLcrFilters}
                                               showProgress={this.state.showProgress}/>

                        <br/>

                        <Filters reportPeriod={this.state.filter.reportPeriod}
                                 onFilterValuesChange={this.onFilterValuesChange}
                                 handleApplyFilters={this.handleApplyFilters}
                                 showProgress={this.state.showProgress}
                                 startShowProgress={this.startShowProgress}
                                 stopShowProgress={this.stopShowProgress}
                                 setStateDescription={this.setStateDescription}/>

                        <br/>

                        <DataPreview filterValues={this.state.dataPreview.filterValues}
                                     reportPeriod={this.state.reportPeriod}
                                     reportType={this.state.dataPreview.reportType}
                                     onReportTypeChange={this.onReportTypeChange}
                                     onExcludedItemsChange={this.onExcludedItemsChange}
                                     showProgress={this.state.showProgress}
                                     startShowProgress={this.startShowProgress}
                                     stopShowProgress={this.stopShowProgress}
                                     setStateDescription={this.setStateDescription}
                                     handleBeginVerification={this.handleBeginVerification}
                                     handleSaveReport={this.handleSaveReport}/>

                    </TabPanel>

                    <TabPanel value={this.state.tab} index={1}>

                        <Report reportPeriod={this.state.reportPeriod}
                                reportType={this.state.report.reportType}
                                filterValues={this.state.report.filterValues}
                                excludedItems={this.state.report.excludedItems}
                                showProgress={this.state.showProgress}
                                startShowProgress={this.startShowProgress}
                                stopShowProgress={this.stopShowProgress}
                                setStateDescription={this.setStateDescription}/>

                    </TabPanel>

                </Container>
            </>
        );
    }
}

ReactDOM.render(
    <App/>,
    document.getElementById('root')
);
