import React from "react";
import TableRow from "@material-ui/core/TableRow/TableRow";
import TableCell from "@material-ui/core/TableCell/TableCell";
import Checkbox from "@material-ui/core/Checkbox/Checkbox";

export default class extends React.Component {

    onExcludeChange = () => {
        this.props.onExcludeItem(this.props.item);
    };

    render() {
        return (
            <TableRow>
                <TableCell>{this.props.item.price}</TableCell>
                <TableCell>{this.props.item.operator}</TableCell>
                <TableCell>{this.props.item.country}</TableCell>
                <TableCell>{this.props.item.product}</TableCell>
                <TableCell>{this.props.item.prefixZone}</TableCell>
                <TableCell>{this.props.item.prefix}</TableCell>
                <TableCell>
                    <Checkbox disabled={true} checked={this.props.item.isHidden}/>
                </TableCell>
                <TableCell>
                    <Checkbox disabled={this.props.item.isHidden || this.props.buttonsDisabled}
                              checked={this.props.item.isExcluded}
                              onChange={this.onExcludeChange}/>
                </TableCell>
                <TableCell>{this.props.item.fromDate}</TableCell>
                <TableCell>{this.props.item.toDate}</TableCell>
            </TableRow>
        )
    }
}