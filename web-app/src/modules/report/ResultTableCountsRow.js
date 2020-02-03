import React from "react";
import TableRow from "@material-ui/core/TableRow/TableRow";
import TableCell from "@material-ui/core/TableCell/TableCell";

export default class extends React.Component {

    render() {
        return (
            <TableRow>
                <TableCell>{this.props.item.prefixZoneItc}</TableCell>
                <TableCell>{this.props.item.prefixZoneLcr}</TableCell>
                <TableCell>{this.props.item.operator}</TableCell>
                <TableCell>{this.props.item.service}</TableCell>
                <TableCell>{this.props.item.countItc}</TableCell>
                <TableCell>{this.props.item.countLcr}</TableCell>
            </TableRow>
        )
    }
}