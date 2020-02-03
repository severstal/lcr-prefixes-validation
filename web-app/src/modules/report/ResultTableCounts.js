import React from "react";
import Table from "@material-ui/core/Table/Table";
import TableHead from "@material-ui/core/TableHead/TableHead";
import TableRow from "@material-ui/core/TableRow/TableRow";
import TableCell from "@material-ui/core/TableCell/TableCell";
import TableBody from "@material-ui/core/TableBody/TableBody";
import ResultTableCountsRow from "./ResultTableCountsRow";
import TableFooter from "@material-ui/core/TableFooter/TableFooter";
import TablePagination from "@material-ui/core/TablePagination/TablePagination";

export default class extends React.Component {

    state = {
        page: 0,
        rowsPerPage: 100
    };

    handleChangePage = (event, newPage) =>{
        if (this.props.disabled) {
            return;
        }
        this.setState({
            page: newPage
        });
    };

    handleChangeRowsPerPage = (event) => {
        if (this.props.disabled) {
            return;
        }
        let rowsPerPage = parseInt(event.target.value);
        this.setState({
            page: 0,
            rowsPerPage: rowsPerPage
        });
    };

    render() {
        const rows = this.props.reportItems
            .slice(this.state.page * this.state.rowsPerPage, this.state.page * this.state.rowsPerPage + this.state.rowsPerPage)
            .map(item => 
                <ResultTableCountsRow key={item.id} item={item} /> 
            );
        
        return (
            <div>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Префиксная зона в Интерконнект</TableCell>
                            <TableCell>Зона в прайсе </TableCell>
                            <TableCell>Оператор</TableCell>
                            <TableCell>Услуга в Интерконнект</TableCell>
                            <TableCell>В Интерконнект</TableCell>
                            <TableCell>В прайсе</TableCell>
                        </TableRow>
                    </TableHead>

                    <TableBody>
                        {rows}
                    </TableBody>

                    <TableFooter>
                        <TableRow>
                            <TablePagination
                                labelRowsPerPage={'Строк на странице'}
                                rowsPerPageOptions={[50, 100, 500]}
                                // component="div"
                                count={this.props.reportItems.length}
                                rowsPerPage={this.state.rowsPerPage}
                                page={this.state.page}
                                backIconButtonProps={{
                                    'aria-label': 'previous page',
                                }}
                                nextIconButtonProps={{
                                    'aria-label': 'next page',
                                }}
                                onChangePage={this.handleChangePage}
                                onChangeRowsPerPage={this.handleChangeRowsPerPage}
                            />
                        </TableRow>
                    </TableFooter>

                </Table>
            </div>
        )
    }
}
