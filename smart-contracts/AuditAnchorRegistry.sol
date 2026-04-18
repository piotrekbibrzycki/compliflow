// SPDX-License-Identifier: MIT
pragma solidity ^0.8.24;

contract AuditAnchorRegistry {
    mapping(bytes32 => bool) private anchoredProofs;

    event AuditProofAnchored(
        bytes32 indexed proofHash,
        uint256 indexed transferId,
        string proofSchemaVersion,
        uint256 anchoredAt,
        address indexed anchoredBy
    );

    function anchorAuditProof(
        bytes32 proofHash,
        uint256 transferId,
        string calldata proofSchemaVersion
    ) external {
        require(proofHash != bytes32(0), "Invalid proof hash");
        require(!anchoredProofs[proofHash], "Proof already anchored");

        anchoredProofs[proofHash] = true;

        emit AuditProofAnchored(
            proofHash,
            transferId,
            proofSchemaVersion,
            block.timestamp,
            msg.sender
        );
    }

    function isProofAnchored(bytes32 proofHash) external view returns (bool) {
        return anchoredProofs[proofHash];
    }
}