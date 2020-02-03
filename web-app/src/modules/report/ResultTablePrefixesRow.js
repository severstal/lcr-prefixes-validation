import React from "react";
import TableRow from "@material-ui/core/TableRow/TableRow";
import TableCell from "@material-ui/core/TableCell/TableCell";

export default class ResultTablePrefixesRow extends React.Component {

    render() {
        return (
            <TableRow>
                <TableCell>{this.props.item.price}</TableCell>
                <TableCell>{this.props.item.operator}</TableCell>
                <TableCell>{this.props.item.service}</TableCell>
                <TableCell>{this.props.item.prefix}</TableCell>
                <TableCell>{this.props.item.prefixInPrefZone}</TableCell>
                <TableCell>{this.props.item.prefixZone}</TableCell>
                <TableCell>{this.props.item.prefixZoneFromLcr}</TableCell>
                <TableCell>{this.props.item.priceLcr}</TableCell>
                <TableCell>{this.props.item.prefixZoneLcr}</TableCell>
                <TableCell>{this.props.item.differenceType}</TableCell>
            </TableRow>
        )
    }
}