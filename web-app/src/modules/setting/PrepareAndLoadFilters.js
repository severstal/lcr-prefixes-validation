import React from "react";
import Grid from "@material-ui/core/Grid/Grid";
import Button from "@material-ui/core/Button/Button";
import {MuiPickersUtilsProvider, KeyboardDatePicker} from '@material-ui/pickers';
import DateFnsUtils from '@date-io/date-fns';
import Tooltip from "@material-ui/core/Tooltip/Tooltip";

export default class extends React.Component {

    handleFromDateChange = (date) => {
        this.props.onReportPeriodChange({toDate: this.props.reportPeriod.toDate, fromDate: date});
    };

    handleToDateChange = (date) => {
        this.props.onReportPeriodChange({toDate: date, fromDate: this.props.reportPeriod.fromDate});
    };

    render() {

        const buttonsDisabled = this.props.showProgress;

        return (
            <>
                <Grid container spacing={1}>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <span>Дата с</span>
                    </Grid>

                    <MuiPickersUtilsProvider utils={DateFnsUtils}>

                        <Grid item style={{display: 'flex', alignItems: 'center'}}>
                            <KeyboardDatePicker
                                disabled={buttonsDisabled}
                                margin="normal"
                                id="date-picker-dialog-from"
                                format="dd.MM.yyyy"
                                value={this.props.reportPeriod.fromDate}
                                KeyboardButtonProps={{'aria-label': 'change date'}}
                                autoOk
                                onChange={this.handleFromDateChange}/>
                        </Grid>

                        <Grid item style={{display: 'flex', alignItems: 'center'}}>
                            <span>по</span>
                        </Grid>

                        <Grid item style={{display: 'flex', alignItems: 'center'}}>
                            <KeyboardDatePicker
                                disabled={buttonsDisabled}
                                margin="normal"
                                id="date-picker-dialog-to"
                                format="dd.MM.yyyy"
                                value={this.props.reportPeriod.toDate}
                                KeyboardButtonProps={{'aria-label': 'change date'}}
                                autoOk
                                onChange={this.handleToDateChange}/>
                        </Grid>

                    </MuiPickersUtilsProvider>

                    <Grid item style={{display: 'flex', alignItems: 'center'}}>
                        <Tooltip title="Загрузить фильтр">
                            <Button onClick={this.props.handleLoadLcrFilters} variant="contained"
                                    color="primary" size="small" disabled={buttonsDisabled}>
                                Начать выгрузку
                            </Button>
                        </Tooltip>
                    </Grid>

                </Grid>
            </>
        );
    }
}
