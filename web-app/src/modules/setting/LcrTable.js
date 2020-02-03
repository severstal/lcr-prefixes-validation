import React from "react";
import Table from "@material-ui/core/Table/Table";
import TableHead from "@material-ui/core/TableHead/TableHead";
import TableRow from "@material-ui/core/TableRow/TableRow";
import TableCell from "@material-ui/core/TableCell/TableCell";
import TableBody from "@material-ui/core/TableBody/TableBody";
import LcrTableRow from './LcrTableRow'
import TableFooter from "@material-ui/core/TableFooter/TableFooter";
import TablePagination from "@material-ui/core/TablePagination/TablePagination";

export default class extends React.Component {

    render() {

        const rows = this.props.items.map(item =>
            <LcrTableRow key={item.id} item={item}
                         buttonsDisabled={this.props.buttonsDisabled}
                         onExcludeItem={this.props.onExcludeItem}/>
        );

        return (
            <>
                <h4>Данные для сверки</h4>
                <Table>
                    <TableHead>
                        <TableRow>
                            <TableCell>Прайс</TableCell>
                            <TableCell>Оператор</TableCell>
                            <TableCell>Страна</TableCell>
                            <TableCell>Продукт</TableCell>
                            <TableCell>Префиксная зона</TableCell>
                            <TableCell>Префикс</TableCell>
                            <TableCell>Свернутые</TableCell>
                            <TableCell>Исключить</TableCell>
                            <TableCell>Дата начала</TableCell>
                            <TableCell>Дата закрытия</TableCell>
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
                                count={this.props.totalItemsCount}
                                rowsPerPage={this.props.pageSize}
                                page={this.props.pageNumber}
                                backIconButtonProps={{
                                    'aria-label': 'previous page',
                                }}
                                nextIconButtonProps={{
                                    'aria-label': 'next page',
                                }}
                                onChangePage={this.props.onPageChange}
                                onChangeRowsPerPage={this.props.onPageSizeChange}
                            />
                        </TableRow>
                    </TableFooter>

                </Table>
            </>
        )
    }
}